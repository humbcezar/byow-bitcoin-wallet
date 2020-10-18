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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.containsRow;
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
        String toAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        String fromAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(101, fromAddress);
        bitcoindRpcClient.sendToAddress(toAddress, BigDecimal.ONE);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        WaitForAsyncUtils.waitFor(20, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return !tableView.getItems().isEmpty();
        });

        TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, containsRowAtIndex(
                0,
                toAddress,
                "1.00000000",
                0
            )
        );
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String expectedReceinvingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/1"));
        assertEquals(expectedReceinvingAddress, address);
    }

    @Test
    public void showTenAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        //TODO: testar com mais de 20
        //TODO: testar com multiplas transacoes para mesmos enderecos
        int numberOfReceivingAddresses = 10;
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
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/" + numberOfReceivingAddresses));
        assertEquals(expectedReceivingAddress, address);
    }
}