package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.AddressPath;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.address.AddressSequentialGenerator;
import byow.bitcoinwallet.services.address.SeedGenerator;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
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

import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TotalBalanceTest extends TestBase {
    public static final int TIMEOUT = 40;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AddressSequentialGenerator addressSequentialGenerator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    private String seed;

    private String walletName;

    private BigDecimalStringConverter bigDecimalStringConverter = new BigDecimalStringConverter();

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
    public void showPositiveBalanceWithOneUnconfirmedBalance(FxRobot robot) throws TimeoutException {
        int numberOfReceivingAddresses = 1;

        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed, FIRST_BIP84_ADDRESS_PATH
        ).stream().map(AddressPath::getAddress).collect(Collectors.toList());
        addresses.forEach(address -> bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("OK");
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            String totalBalance = robot.lookup("#totalBalance").queryAs(Label.class).getText();
            return tableView.getItems().size() == numberOfReceivingAddresses && !totalBalance.isBlank();
        });
        String totalBalance = robot.lookup("#totalBalance").queryAs(Label.class).getText();
        BigDecimal expectedBalance = BigDecimal.ONE;
        assertEquals(
            String.format(
                "Total Balance: %s BTC (confirmed: %s, unconfirmed: %s)",
                bigDecimalStringConverter.toString(expectedBalance).concat(".00000000"),
                bigDecimalStringConverter.toString(BigDecimal.ZERO),
                bigDecimalStringConverter.toString(expectedBalance).concat(".00000000")
            ),
            totalBalance
        );
    }

    @Test
    public void showPositiveBalanceWithTwoUtxosOneAddressOneUnconfirmedAnotherConfirmed(FxRobot robot) throws TimeoutException {
        int numberOfReceivingAddresses = 1;

        List<String> addresses = addressSequentialGenerator.deriveAddresses(
            numberOfReceivingAddresses,
            seed, FIRST_BIP84_ADDRESS_PATH
        ).stream()
        .map(AddressPath::getAddress)
        .collect(Collectors.toList());
        addresses.forEach(address -> bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(1, nodeAddress);
        addresses.forEach(address -> bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("OK");
        WaitForAsyncUtils.waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            String totalBalance = robot.lookup("#totalBalance").queryAs(Label.class).getText();
            return tableView.getItems().size() == numberOfReceivingAddresses && !totalBalance.isBlank();
        });
        String totalBalance = robot.lookup("#totalBalance").queryAs(Label.class).getText();
        assertEquals(
            String.format(
                "Total Balance: %s BTC (confirmed: %s, unconfirmed: %s)",
                bigDecimalStringConverter.toString(new BigDecimal(2)).concat(".00000000"),
                bigDecimalStringConverter.toString(BigDecimal.ONE).concat(".00000000"),
                bigDecimalStringConverter.toString(BigDecimal.ONE).concat(".00000000")
            ),
            totalBalance
        );
    }
}
