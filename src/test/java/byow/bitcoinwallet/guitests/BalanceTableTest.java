package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.DerivationPath;
import byow.bitcoinwallet.services.SeedGenerator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.containsRowAtIndex;


public class BalanceTableTest extends TestBase {

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private WalletRepository walletRepository;

    private String seed;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        Wallet wallet = new Wallet("testwallet2", seed);
        walletRepository.save(wallet);
        super.start(stage);
    }

    @Test
    public void showAddressWithPositiveBalance(FxRobot robot) {
        String toAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        String fromAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(101, fromAddress);
        bitcoindRpcClient.sendToAddress(toAddress, BigDecimal.ONE);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn("testwallet2");
        //TODO: substitute sleep for something safer
        robot.sleep(5, TimeUnit.SECONDS);
        final TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, containsRowAtIndex(
                0,
                toAddress,
                "1.00000000",
                0
            )
        );
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String expectedReceinvingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/1"));
        assertEquals(expectedReceinvingAddress, address);
    }
}
