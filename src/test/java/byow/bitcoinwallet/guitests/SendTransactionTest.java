package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.SeedGenerator;
import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void sendOneTransaction(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot);
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
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) < 0;
        });
        TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), tableView.getItems().get(0).getBigDecimalBalance().setScale(1, RoundingMode.HALF_UP));

        BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
        assertEquals(new BigDecimal("0.50000000"), nodeAddressBalance);
    }
    //TODO: transferir para outra wallet do byow, ver se change e output da outra wallet eh spendable, confirmar, setar num minimo de confs?

    private void fundWallet(FxRobot robot, String firstAddress, BigDecimal amount, int confirmations) throws TimeoutException {
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        bitcoindRpcClient.sendToAddress(address, amount);
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(confirmations, nodeAddress);
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == confirmations;
        });
    }
}
