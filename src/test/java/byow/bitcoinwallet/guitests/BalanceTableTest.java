package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.AddressSequentialGenerator;
import byow.bitcoinwallet.services.DerivationPath;
import byow.bitcoinwallet.services.SeedGenerator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.*;

public class BalanceTableTest extends TestBase {

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AddressSequentialGenerator addressSequentialGenerator;

    private String seed;

    private String walletName;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        walletName = RandomString.make();
        seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        Wallet wallet = new Wallet(walletName, seed);
        walletRepository.save(wallet);
        super.start(stage);
    }

    @Test
    public void showAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 1);
    }

    @Test
    public void showTenAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 10);
    }

    @Test
    public void showTwentyFiveAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 25);
    }

    @Test
    public void showThreeAddressesWithPositiveValueWithVaryingTransactions(FxRobot robot) throws TimeoutException {
        int numberOfReceivingAddresses = 3;

        List<String> addresses = addressSequentialGenerator.deriveAddresses(numberOfReceivingAddresses, seed, FIRST_BIP84_ADDRESS_PATH);
        String fromAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(101, fromAddress);

        //balance:4, confirmations:11,
        bitcoindRpcClient.sendToAddress(addresses.get(0), BigDecimal.ONE);
        bitcoindRpcClient.sendToAddress(addresses.get(0), new BigDecimal(3));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        //balance:10, confirmations:9,
        bitcoindRpcClient.sendToAddress(addresses.get(1), BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        bitcoindRpcClient.sendToAddress(addresses.get(1), new BigDecimal(2));
        bitcoindRpcClient.sendToAddress(addresses.get(1), new BigDecimal(7));
        //balance:5, confirmations:5,
        bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(5, fromAddress);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == numberOfReceivingAddresses;
        });

        TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);

        MatcherAssert.assertThat(tableView, containsRow(
                addresses.get(0),
                "4.00000000",
                11
            )
        );
        MatcherAssert.assertThat(tableView, containsRow(
                addresses.get(1),
                "10.00000000",
                9
            )
        );
        MatcherAssert.assertThat(tableView, containsRow(
                addresses.get(2),
                "5.00000000",
                5
            )
        );
        MatcherAssert.assertThat(tableView, hasNumRows(numberOfReceivingAddresses));
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/" + numberOfReceivingAddresses));
        assertEquals(expectedReceivingAddress, address);
    }

    private void showNAddressesWithPositiveBalance(FxRobot robot, int numberOfReceivingAddresses) throws TimeoutException {
        List<String> addresses = addressSequentialGenerator.deriveAddresses(numberOfReceivingAddresses, seed, FIRST_BIP84_ADDRESS_PATH);
        String fromAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(101, fromAddress);

        addresses.forEach(address -> bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == numberOfReceivingAddresses;
        });

        TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);

        IntStream.range(0, numberOfReceivingAddresses).boxed().forEach(i -> {
            MatcherAssert.assertThat(tableView, containsRow(
                    addresses.get(i),
                    "1.00000000",
                    0
                )
            );
        });
        MatcherAssert.assertThat(tableView, hasNumRows(numberOfReceivingAddresses));
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/" + numberOfReceivingAddresses));
        assertEquals(expectedReceivingAddress, address);
    }

    //TODO: testar com floating btc recebidos
}