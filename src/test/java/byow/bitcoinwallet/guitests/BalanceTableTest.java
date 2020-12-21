package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.Address;
import byow.bitcoinwallet.entities.NextChangeAddress;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_CHANGE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.*;

public class BalanceTableTest extends TestBase {

    public static final int TIMEOUT = 40;

    @Autowired
    private NextChangeAddress nextChangeAddress;

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
        wallet.setCreatedAt(new Date());
        walletRepository.save(wallet);
        super.start(stage);
    }

    @Test
    public void showAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 1, FIRST_BIP84_ADDRESS_PATH);
        assertNextReceivingAddress(robot, 1);
        assertNextChangeAddress(0);
    }

    @Test
    public void showTenAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 10, FIRST_BIP84_ADDRESS_PATH);
        assertNextReceivingAddress(robot, 10);
        assertNextChangeAddress(0);
    }

    @Test
    public void showTwentyFiveAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 25, FIRST_BIP84_ADDRESS_PATH);
        assertNextReceivingAddress(robot, 25);
        assertNextChangeAddress(0);
    }

    @Test
    public void showAddressWithPositiveBalanceWithChangeAddresses(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 1, FIRST_BIP84_CHANGE_PATH);
        assertNextChangeAddress(1);
        assertNextReceivingAddress(robot, 0);
    }

    @Test
    public void showTenAddressWithPositiveBalanceWithChangeAddresses(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 10, FIRST_BIP84_CHANGE_PATH);
        assertNextChangeAddress(10);
        assertNextReceivingAddress(robot, 0);
    }

    @Test
    public void showTwentyFiveAddressWithPositiveBalanceWithChangeAddresses(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 25, FIRST_BIP84_CHANGE_PATH);
        assertNextChangeAddress(25);
        assertNextReceivingAddress(robot, 0);
    }

    @Test
    public void showThreeAddressesWithPositiveValueWithVaryingTransactionsWithChangeAddresses(FxRobot robot) throws TimeoutException {
        int numberOfReceivingAddresses = 3;

        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed, FIRST_BIP84_CHANGE_PATH
        ).stream().map(Address::getAddress).collect(Collectors.toList());
        String fromAddress = bitcoindRpcClient.getNewAddress();

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
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
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
        assertNextChangeAddress(numberOfReceivingAddresses);
        assertNextReceivingAddress(robot, 0);
    }

    @Test
    public void showThreeAddressesWithPositiveValueWithVaryingTransactions(FxRobot robot) throws TimeoutException {
        int numberOfReceivingAddresses = 3;

        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed, FIRST_BIP84_ADDRESS_PATH
        ).stream().map(Address::getAddress).collect(Collectors.toList());
        String fromAddress = bitcoindRpcClient.getNewAddress();

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
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
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
        assertNextReceivingAddress(robot, numberOfReceivingAddresses);
        assertNextChangeAddress(0);
    }

    private void showNAddressesWithPositiveBalance(FxRobot robot, int numberOfReceivingAddresses, DerivationPath firstDerivationPath) throws TimeoutException {
        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed,
                firstDerivationPath
        ).stream().map(Address::getAddress).collect(Collectors.toList());

        addresses.forEach(address -> bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
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
    }

    private void assertNextReceivingAddress(FxRobot robot, int numberOfReceivingAddresses) throws TimeoutException {
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/" + numberOfReceivingAddresses));
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && address.equals(expectedReceivingAddress);
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedReceivingAddress, address);
    }

    private void assertNextChangeAddress(int numberOfReceivingAddresses) throws TimeoutException {
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/1/" + numberOfReceivingAddresses));
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String address =  nextChangeAddress.getValue().getAddress();
            return address != null && address.equals(expectedReceivingAddress);
        });
        String address =  nextChangeAddress.getValue().getAddress();
        assertEquals(expectedReceivingAddress, address);
    }

    //TODO: testar com floating btc recebidos
}