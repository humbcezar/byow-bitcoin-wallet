package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.SeedGenerator;
import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeoutException;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.lang.Integer.MAX_VALUE;
import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void sendOneTransactionToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String firstAddress = addressGenerator.generate(seedGenerator.generateSeed(mnemonicSeed, ""), FIRST_BIP84_ADDRESS_PATH);
        fundWallet(robot, firstAddress, BigDecimal.ONE, 1);
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.5");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");
        robot.clickOn("OK");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) < 0;
        });
        TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), tableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));

        BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
        assertEquals(new BigDecimal("0.50000000"), nodeAddressBalance);
    }

    @Test
    public void sendOneTransactionToAnotherByowWalletThenSpendIt(FxRobot robot) throws TimeoutException {
        String recipientWallet = RandomString.make();
        String recipientMnemonicSeed = walletUtil.createWallet(robot, recipientWallet);
        String recipientWalletAddress = addressGenerator.generate(
            seedGenerator.generateSeed(recipientMnemonicSeed, ""), FIRST_BIP84_ADDRESS_PATH
        );
        waitFor(60, SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && address.equals(recipientWalletAddress);
        });

        String senderMnemonicSeed = walletUtil.createWallet(robot, RandomString.make());
        String senderWalletAddress = addressGenerator.generate(
            seedGenerator.generateSeed(senderMnemonicSeed, ""), FIRST_BIP84_ADDRESS_PATH
        );
        fundWallet(robot, senderWalletAddress, BigDecimal.ONE, 1);

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


        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.25");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");
        robot.clickOn("OK");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1
                    && tableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.25")) < 0;
        });

        TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.25"), tableView.getItems().get(0).getBigDecimalBalance().setScale(2, HALF_UP));

        BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
        assertEquals(new BigDecimal("0.25000000"), nodeAddressBalance);
    }
    //TODO: transferir para outra wallet do byow, ver se change e output da outra wallet eh spendable, confirmar, setar num minimo de confs?
    //TODO: travar ou cancelar criacao/load de outra wallet enquanto carrega/cria uma

    private void fundWallet(FxRobot robot, String firstAddress, BigDecimal amount, int confirmations) throws TimeoutException {
        waitFor(40, SECONDS, () -> {
            robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        bitcoindRpcClient.sendToAddress(address, amount);
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(confirmations, nodeAddress);
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == confirmations;
        });
    }
}
