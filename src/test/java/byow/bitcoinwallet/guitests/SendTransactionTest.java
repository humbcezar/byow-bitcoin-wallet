package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.SeedGenerator;
import byow.bitcoinwallet.services.TotalBalanceCalculator;
import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.util.concurrent.TimeoutException;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_CHANGE_PATH;
import static java.lang.Integer.MAX_VALUE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class SendTransactionTest extends TestBase {

    @Autowired
    private WalletUtil walletUtil;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private TotalBalanceCalculator totalBalanceCalculator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void sendOneTransactionToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1);
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0);
    }

    @Test
    public void sendFiveTransactionsToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, valueOf(5), 1);
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.8", 1, "0.80000000", 5, seed, 0);
    }

    @Test
    public void sendFiveTransactionsFromWalletWithThreeUtxosToNodeAddress(FxRobot robot) {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");

        fundNAddresses(robot, seed, valueOf(2), 1, 3);
        sendNTransactions(robot, "0.8", 1, "0.80000000", 5);
    }

    @Test
    public void sendOneTransactionFromWalletWithThreeUtxosToNodeAddress(FxRobot robot) {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");

        fundNAddresses(robot, seed, valueOf(2), 1, 3);
        sendNTransactions(robot, "5", 1, "5.00000000", 1);
    }

    @Test
    public void sendOneTransactionWithoutEnoughFundsFail(FxRobot robot) {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");

        fundNAddresses(robot, seed, valueOf(2), 1, 1);

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("5");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");
        robot.clickOn("OK");

        NodeQuery text = robot.lookup("Not enough available funds for transaction.");
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }
    //TODO: incluir fees na regra acima

    private void sendNTransactions(FxRobot robot, String amount, int scale, String expectedBalance, int numberOfTransactions) {
        range(0, numberOfTransactions).forEach(i -> {
            BigDecimal previousBalance = totalBalanceCalculator.getTotalBalance();

            String nodeAddress = bitcoindRpcClient.getNewAddress();
            robot.clickOn("#sendTab");
            robot.clickOn("#amountToSend");
            robot.write(amount);
            robot.clickOn("#addressToSend");
            robot.write(nodeAddress);
            robot.clickOn("#send");
            robot.clickOn("OK");
            try {
                waitFor(60, SECONDS, () ->
                    previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP)
                        .equals(totalBalanceCalculator.getTotalBalance().setScale(scale, HALF_UP))
                );
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            assertEquals(
                previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP),
                totalBalanceCalculator.getTotalBalance().setScale(scale, HALF_UP)
            );

            BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
            assertEquals(new BigDecimal(expectedBalance), nodeAddressBalance);
        });
    }

    @Test
    public void sendOneTransactionToAnotherByowWalletThenSpendIt(FxRobot robot) throws TimeoutException {
        String recipientWallet = RandomString.make();
        String recipientMnemonicSeed = walletUtil.createWallet(robot, recipientWallet);
        String recipientSeed = seedGenerator.generateSeed(recipientMnemonicSeed, "");
        String recipientWalletAddress = addressGenerator.generate(recipientSeed, FIRST_BIP84_ADDRESS_PATH);
        waitFor(60, SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && address.equals(recipientWalletAddress);
        });

        String senderMnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String seed = seedGenerator.generateSeed(senderMnemonicSeed, "");
        String senderWalletAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, senderWalletAddress, ONE, 1);

        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.5");
        robot.clickOn("#addressToSend");
        robot.write(recipientWalletAddress);
        robot.clickOn("#send");
        robot.clickOn("OK");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 &&
                tableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) < 0;
        });
        TableView<ReceivingAddress> senderTableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), senderTableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(recipientWallet);
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> recipientTableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return recipientTableView.getItems().size() == 1 &&
                recipientTableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) == 0;
        });
        TableView<ReceivingAddress> recipientTableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), recipientTableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));

        sendNTransactions(robot, "0.25", 2, "0.25000000", 1, recipientSeed, 0);
    }

    private void fundAddress(FxRobot robot, String firstAddress, BigDecimal amount, int confirmations) throws TimeoutException {
        waitFor(40, SECONDS, () -> {
            robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        bitcoindRpcClient.sendToAddress(address, amount);
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(confirmations, nodeAddress);
    }

    private void fundNAddresses(
        FxRobot robot,
        String seed,
        BigDecimal amount,
        int confirmations,
        int numberOfAddresses
    ) {
        range(0, numberOfAddresses).forEach(i -> {
            String address = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(i));
            try {
                fundAddress(robot, address, amount, confirmations);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            waitFor(60, SECONDS, () -> {
                TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
                return tableView.getItems().size() == numberOfAddresses
                        && tableView.getItems().get(0).getConfirmations() == numberOfAddresses;
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNTransactions(
        FxRobot robot,
        String amount,
        int scale,
        String expectedBalance,
        int numTransactions,
        String seed,
        int firstChangeIndex
    ) {
        range(0, numTransactions).forEach(i -> {
            TableView<ReceivingAddress> table = robot.lookup("#balanceTable").queryAs(TableView.class);
            BigDecimal previousBalance = table.getItems().get(0).getBigDecimalBalance();

            String nodeAddress = bitcoindRpcClient.getNewAddress();
            robot.clickOn("#sendTab");
            robot.clickOn("#amountToSend");
            robot.write(amount);
            robot.clickOn("#addressToSend");
            robot.write(nodeAddress);
            robot.clickOn("#send");
            robot.clickOn("OK");
            try {
                waitFor(60, SECONDS, () -> {
                    TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
                    return !isNull(tableView) && tableView.getItems().size() == 1
                            && tableView.getItems().get(0).getBigDecimalBalance().compareTo(previousBalance) < 0;
                });
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            assertEquals(
                previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP),
                table.getItems().get(0).getBigDecimalBalance().setScale(scale, HALF_UP)
            );

            assertEquals(
                addressGenerator.generate(seed, FIRST_BIP84_CHANGE_PATH.next(i + firstChangeIndex)),
                table.getItems().get(0).getAddress()
            );

            BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
            assertEquals(new BigDecimal(expectedBalance), nodeAddressBalance);
        });
    }
}
