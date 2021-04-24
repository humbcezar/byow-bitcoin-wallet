package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.TransactionRow;
import byow.bitcoinwallet.services.address.AddressGenerator;
import byow.bitcoinwallet.services.address.NestedSegwitAddressGeneratorBySeed;
import byow.bitcoinwallet.services.address.SeedGenerator;
import byow.bitcoinwallet.services.wallet.TotalBalanceCalculator;
import byow.bitcoinwallet.utils.TransactionInfo;
import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.ProgressBar;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static byow.bitcoinwallet.services.address.DerivationPath.*;
import static java.lang.Integer.MAX_VALUE;
import static java.math.BigDecimal.valueOf;
import static java.math.BigDecimal.*;
import static java.math.RoundingMode.*;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;
import static net.bytebuddy.utility.RandomString.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class SendTransactionTest extends TestBase {

    private static final long TIMEOUT = 60;

    @Autowired
    private WalletUtil walletUtil;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private NestedSegwitAddressGeneratorBySeed nestedSegwitAddressGeneratorBySeed;

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
        String password = make();
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), password);
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, password);
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "bech32", 1, password);
    }

    @Test
    public void sendOneTransactionWithWrongPasswordFail(FxRobot robot) throws TimeoutException {
        String password = make();
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), password);
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, password);
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        sendBtc(robot, "0.5", "wrong", nodeAddress);
        NodeQuery text = robot.lookup("Wrong password.");
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }

    @Test
    public void sendOneTransactionToNodeAddressFromNestedSegwitUtxo(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = nestedSegwitAddressGeneratorBySeed.generate(seed, FIRST_BIP49_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#nestedReceivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "bech32", 1, "");
    }

    @Test
    public void sendOneTransactionToNodeAddressFromNestedSegwitUtxoAndDefaultUtxo(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = nestedSegwitAddressGeneratorBySeed.generate(seed, FIRST_BIP49_ADDRESS_PATH);
        String secondAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#nestedReceivingAddress");
        fundAddress(robot, secondAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 2 && tableView.getItems().get(0).getConfirmations() == 1
                    && tableView.getItems().get(1).getConfirmations() == 2;
        });

        sendNTransactions(robot, "1.5", 1, "1.50000000", 1, seed, 0, "bech32", 2, "");
    }

    @Test
    public void sendFiveTransactionsToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, BigDecimal.valueOf(5), 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(
            robot,
            "0.8",
            1,
            "0.80000000",
            5,
            seed,
            0,
            "bech32",
            1,
            ""
        );
    }

    @Test
    public void sendFiveTransactionsWithSameAddressInputsToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        waitFor(40, SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        range(0, 5).forEach((int i) -> {
            robot.clickOn("#addressesTab");
            bitcoindRpcClient.sendToAddress(address, ONE);
            String nodeAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(1, nodeAddress);
        });
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getBalance().equals("5.00000000");
        });

        sendNTransactions(robot, "4", 1, "4.00000000", 1, seed, 0, "bech32", 5, "");
    }

    @Test
    public void sendFiveTransactionsFromWalletWithThreeUtxosToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");

        fundNAddresses(robot, seed, BigDecimal.valueOf(2), 1, 3);
        BigDecimal initialBalance = new BigDecimal(6);
        sendNTransactions(robot, "0.8", 1, "0.80000000", 5, 3, initialBalance);
    }

    @Test
    public void sendOneTransactionFromWalletWithThreeUtxosToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");

        fundNAddresses(robot, seed, BigDecimal.valueOf(2), 1, 3);
        BigDecimal initialBalance = new BigDecimal(6);
        sendNTransactions(robot, "5", 1, "5.00000000", 1, 3, initialBalance);
    }

    @Test
    public void sendOneTransactionWithoutEnoughFundsFail(FxRobot robot) {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");

        fundNAddresses(robot, seed, BigDecimal.valueOf(2), 1, 1);

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("5");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Not enough funds available for transaction.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionToAnotherByowWalletThenSpendIt(FxRobot robot) throws TimeoutException {
        String recipientWallet = RandomString.make();
        String recipientMnemonicSeed = walletUtil.createWallet(robot, recipientWallet, "");
        String recipientSeed = seedGenerator.generateSeedAsString(recipientMnemonicSeed, "");
        String recipientWalletAddress = addressGenerator.generate(recipientSeed, FIRST_BIP84_ADDRESS_PATH);
        waitFor(TIMEOUT, SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && address.equals(recipientWalletAddress);
        });

        String senderMnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(senderMnemonicSeed, "");
        String senderWalletAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, senderWalletAddress, ONE, 1, "#receivingAddress");

        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.5");
        robot.clickOn("#addressToSend");
        robot.write(recipientWalletAddress);
        robot.clickOn("#send");
        robot.clickOn("OK");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 &&
                tableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) < 0;
        });
        TableView<ReceivingAddress> senderTableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), senderTableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(1, nodeAddress);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(recipientWallet);
        robot.clickOn("OK");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> recipientTableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            ProgressBar progressBar = robot.lookup("#progressBar").queryAs(ProgressBar.class);
            return recipientTableView.getItems().size() == 1 &&
                recipientTableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) == 0 &&
                !progressBar.isIndeterminate();
        });
        TableView<ReceivingAddress> recipientTableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), recipientTableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));

        sendNTransactions(robot, "0.25", 2, "0.25000000", 1, recipientSeed, 0, "bech32", 1, "");
        //TODO: esquematizar novamente esquema de associar tx com wallet, pois qdo wallet recebida eh a msm, da pau nesse teste (input como output, por ex)
        //TODO: arrumar waitfor comparativo (0.5 < 0)
        //TODO: ver se qdo voltar para wallet anterior, tx nao vai estar cagada
    }

    @Test
    public void sendOneTransactionWithDustOutputToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.00000293");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Unable to send the transaction: the transaction has an output lower than the dust limit.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionWithDustChangeToNodeAddressWithInsufficientFundsForChangeFail(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.99999999");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Insufficient funds for calculated fee.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionWithDustChangeToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            BigDecimal previousBalance = totalBalanceCalculator.getTotalBalance();
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1
                && previousBalance.equals(new BigDecimal("1.00000000"));
        });

        //1 - totalFee(which is 2679) - 200 sats -> generating dust change
        sendNTransactions(robot, "0.99997121", 4, "0.99997121", 1, 1, ONE);
    }

    @Test
    public void sendOneTransactionWithInputsEqualAdjustedTargetToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            BigDecimal previousBalance = totalBalanceCalculator.getTotalBalance();
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1
                && previousBalance.equals(new BigDecimal("1.00000000"));
        });

        //1 - totalFee(which is 2679)
        sendNTransactions(robot, "0.99997321", 4, "0.99997321", 1, 1, ONE);
    }

    @Test
    public void sendOneTransactionWithInputsEqualTargetToNodeAddressFail(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("1");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Insufficient funds for calculated fee.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionWithTotalFeeGreaterThanDustButLesserThanIntendedFee(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, valueOf(1.00000300), 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("1");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Insufficient funds for calculated fee.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionToNestedSegwitNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "p2sh-segwit", 1, "");
    }

    @Test
    public void sendOneTransactionToLegacyNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "legacy", 1, "");
    }

    @Test
    public void sendOneTransactionToItsOwnFirstAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendBtc(robot, "0.5", "", firstAddress);
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 2;
        });
        TableView<ReceivingAddress> table = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(
            new BigDecimal("0.5"),
            table.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP)
        );
        assertEquals(
            new BigDecimal("0.5"),
            table.getItems().get(1).getBigDecimalBalance().setScale(1, HALF_UP)
        );
        changeAddressAssertion(seed, 0, 0, table, 1);
    }

    @Test
    public void sendFailForWatchOnlyWallet(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWatchOnlyWallet(robot, RandomString.make(), "", stage);
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.5");
        robot.clickOn("#addressToSend");
        robot.write(firstAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Cannot send transaction for watch only wallet.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendFromWalletWithChild(FxRobot robot) throws TimeoutException {
        String walletName = RandomString.make();
        String mnemonicSeed = walletUtil.createWatchOnlyWallet(robot, walletName, "", stage);

        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("OK");
        waitFor(40, SECONDS, () ->
            "BYOW Wallet - ".concat(walletName).equals(stage.getTitle())
        );
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });
        TransactionInfo transactionInfo = sendNTransactions(
            robot,
            "0.5",
            1,
            "0.50000000",
            1,
            seed,
            0,
            "bech32",
            1,
            ""
        );

        loadWalletAndAssert(robot, walletName.concat("(watch only)"), transactionInfo);
    }

    private void loadWalletAndAssert(FxRobot robot, String walletName, TransactionInfo transactionInfo) throws TimeoutException {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("OK");
        waitFor(40, SECONDS, () ->
            "BYOW Wallet - ".concat(walletName).equals(stage.getTitle())
        );

        robot.clickOn("#addressesTab");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return transactionInfo.getAddressRow().size() == tableView.getItems().size();
        });

        TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        IntStream.range(0, transactionInfo.getAddressRow().size()).forEach(i -> {
            assertEquals(tableView.getItems().get(i).getAddress(), transactionInfo.getAddressRow().get(i).getAddress());
            assertEquals(tableView.getItems().get(i).getBalance(), transactionInfo.getAddressRow().get(i).getBalance());
            assertEquals(tableView.getItems().get(i).getConfirmations(), transactionInfo.getAddressRow().get(i).getConfirmations());
        });

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        transactionsTable.getItems().forEach((transactionRow) -> {
            assertEquals(transactionInfo.getTransactionRows().get(transactionRow.getTransactionId()).getTransactionId(), transactionRow.getTransactionId());
            assertEquals(transactionInfo.getTransactionRows().get(transactionRow.getTransactionId()).getBalance(), transactionRow.getBalance());
            assertEquals(transactionInfo.getTransactionRows().get(transactionRow.getTransactionId()).getConfirmations(), transactionRow.getConfirmations());
        });
    }

    private void fundAddress(FxRobot robot, String firstAddress, BigDecimal amount, int confirmations, String fxmlAddress) throws TimeoutException {
        robot.clickOn("#addressesTab");
        waitFor(40, SECONDS, () -> {
            String address = robot.lookup(fxmlAddress).queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        String address = robot.lookup(fxmlAddress).queryAs(TextField.class).getText();
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
        robot.clickOn("#addressesTab");
        range(0, numberOfAddresses).forEach(i -> {
            String address = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(i));
            try {
                fundAddress(robot, address, amount, confirmations, "#receivingAddress");
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            waitFor(TIMEOUT, SECONDS, () -> {
                TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
                return tableView.getItems().size() == numberOfAddresses
                        && tableView.getItems().get(0).getConfirmations() == numberOfAddresses;
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private TransactionInfo sendNTransactions(
        FxRobot robot,
        String amount,
        int scale,
        String expectedBalance,
        int numTransactions,
        String seed,
        int firstChangeIndex,
        String toAddressType,
        int numPreviousTransactions,
        String password
    ) throws TimeoutException {
        TransactionInfo transactionInfo = new TransactionInfo();
        List<String> transactionAmountsSent = range(0, numTransactions).mapToObj(i -> {
            TableView<ReceivingAddress> table = robot.lookup("#addressesTable").queryAs(TableView.class);
            BigDecimal previousBalance = table.getItems().stream().map(ReceivingAddress::getBigDecimalBalance).reduce(BigDecimal::add).orElse(ZERO);

            String nodeAddress = bitcoindRpcClient.getNewAddress("", toAddressType);
            sendBtc(robot, amount, password, nodeAddress);
            addressesTableAssertion(robot, amount, scale, table, previousBalance);

            changeAddressAssertion(seed, firstChangeIndex, i, table, 0);

            String nodeConfirmationAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(1, nodeConfirmationAddress);
            TableView<ReceivingAddress> tableAfter = robot.lookup("#addressesTable").queryAs(TableView.class);
            try {
                waitFor(TIMEOUT, SECONDS, () -> tableAfter.getItems().stream().allMatch(item -> item.getConfirmations() > 0));
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }

            BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
            assertEquals(new BigDecimal(expectedBalance), nodeAddressBalance);

            return previousBalance.subtract(table.getItems().get(0).getBigDecimalBalance()).toString();
        }).collect(Collectors.toList());

        TableView<ReceivingAddress> table = robot.lookup("#addressesTable").queryAs(TableView.class);
        table.getItems().forEach(row -> {
            transactionInfo.addAddressesRow(row.getAddress(), row.getBalance(), row.getConfirmations());
        });

        robot.clickOn("#transactionsTab");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
            return transactionsTable.getItems().size() == (numTransactions + numPreviousTransactions);
        });
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        assertThat(transactionsTable, hasNumRows(numTransactions + numPreviousTransactions));
        range(0, numTransactions).forEach(i ->
            assertEquals(
                "-".concat(transactionAmountsSent.get(i)),
                transactionsTable.getItems().get(i + numPreviousTransactions).getBigDecimalBalance().setScale(8, FLOOR).toString()
            )
        );

        Map<String, TransactionRow> trRowMap = new HashMap<>();
        transactionsTable.getItems().forEach(transactionRow -> {
            trRowMap.put(transactionRow.getTransactionId(), transactionRow);
        });
        transactionInfo.setTransactionRows(trRowMap);

        return transactionInfo;
    }

    private void changeAddressAssertion(String seed, int firstChangeIndex, int offset, TableView<ReceivingAddress> table, int row) {
        try {
            waitFor(TIMEOUT, SECONDS, () -> table.getItems().size() == row + 1);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        assertEquals(
            addressGenerator.generate(seed, FIRST_BIP84_CHANGE_PATH.next(offset + firstChangeIndex)),
            table.getItems().get(row).getAddress()
        );
    }

    private void addressesTableAssertion(FxRobot robot, String amount, int scale, TableView<ReceivingAddress> table, BigDecimal previousBalance) {
        try {
            waitFor(TIMEOUT, SECONDS, () -> {
                TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
                return !isNull(tableView) && tableView.getItems().size() == 1
                        && previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP).equals(
                            tableView.getItems().get(0).getBigDecimalBalance().setScale(scale, HALF_UP)
                    );
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        assertEquals(
            previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP),
            table.getItems().get(0).getBigDecimalBalance().setScale(scale, HALF_UP)
        );
    }

    private void sendBtc(FxRobot robot, String amount, String password, String address) {
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write(amount);
        robot.clickOn("#addressToSend");
        robot.write(address);
        robot.clickOn("#send");
        robot.clickOn("#sendTransactionPassword");
        robot.write(password);
        robot.clickOn("OK");
    }

    private void sendNTransactions(
        FxRobot robot,
        String amount,
        int scale,
        String expectedBalance,
        int numberOfTransactions,
        int numberOfPreviousTransactions,
        BigDecimal initialBalance) throws TimeoutException {
        List<String> transactionAmountsSent = range(0, numberOfTransactions).mapToObj(i -> {
            try {
                waitFor(TIMEOUT, SECONDS, () -> initialBalance
                    .subtract(new BigDecimal(i)
                    .multiply(new BigDecimal(amount)))
                    .setScale(2, HALF_DOWN)
                    .equals(totalBalanceCalculator.getTotalBalance().setScale(2, HALF_DOWN))
                );
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
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
                waitFor(TIMEOUT, SECONDS, () ->
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

            String nodeConfirmationAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(1, nodeConfirmationAddress);

            BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
            assertEquals(new BigDecimal(expectedBalance), nodeAddressBalance);

            return previousBalance.subtract(totalBalanceCalculator.getTotalBalance()).toString();
        }).collect(Collectors.toList());

        robot.clickOn("#transactionsTab");
        waitFor(TIMEOUT, SECONDS, () -> {
            TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
            return transactionsTable.getItems().size() == (numberOfTransactions + numberOfPreviousTransactions);
        });
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        assertThat(transactionsTable, hasNumRows(numberOfTransactions + numberOfPreviousTransactions));
        range(0, numberOfTransactions).forEach(i ->
            assertEquals(
                "-".concat(transactionAmountsSent.get(i)),
                transactionsTable.getItems().get(i + numberOfPreviousTransactions).getBigDecimalBalance().setScale(8, FLOOR).toString()
            )
        );
    }

}
