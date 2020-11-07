package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.DerivationPath;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReceivingHashBlockTest extends TestBase {

    @Autowired
    private WalletUtil walletUtil;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private AddressGenerator addressGenerator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void receiveOneHashBlock(FxRobot robot) throws TimeoutException, InterruptedException {
        walletUtil.createWallet(robot);
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        robot.sleep(5000);
        bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE);

        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1;
        });

        String randomAddress = addressGenerator.generate(
            seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), ""),
            FIRST_BIP84_ADDRESS_PATH
        );
        bitcoindRpcClient.generateToAddress(1, randomAddress);

        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().get(0).getConfirmations() == 1;
        });
        TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        assertEquals(1, tableView.getItems().get(0).getConfirmations());
    }
}
