package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressSequentialGenerator;
import byow.bitcoinwallet.services.SeedGenerator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.BigDecimalStringConverter;
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
import static org.testfx.matcher.control.TableViewMatchers.containsRow;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;

public class ReceivingTransactionTest extends TestBase {
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private AddressSequentialGenerator addressSequentialGenerator;

    private BigDecimalStringConverter bigDecimalStringConverter = new BigDecimalStringConverter();

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void receiveOneTransaction(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        receiveNTransactions(robot, mnemonicSeed, 1);
    }

    @Test
    public void receiveFiveTransactions(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        receiveNTransactions(robot, mnemonicSeed, 5);
    }

    @Test
    public void receiveFifteenTransactions(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        receiveNTransactions(robot, mnemonicSeed, 15);
    }

    @Test
    public void receiveTwentyTransactions(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        receiveNTransactions(robot, mnemonicSeed, 20);
    }

    @Test
    public void receiveTwentyFiveTransactions(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        receiveNTransactions(robot, mnemonicSeed, 25);
    }

    @Test
    public void receiveTenSequentialTransactionsToTheSameAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        int numberOfTransactions = 10;
        receiveNSequentialTransactions(robot, mnemonicSeed, numberOfTransactions);
    }

    @Test
    public void receiveTwentyTwoSequentialTransactionsToTheSameAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);
        int numberOfTransactions = 22;
        receiveNSequentialTransactions(robot, mnemonicSeed, numberOfTransactions);
    }

    @Test
    public void receiveFiveSequentialTransactionsToTheSameAddressWithDifferentValuesAndConfirmations(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = createWallet(robot);

        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();

        List<ReceivingAddress> addresses = List.of(
                new ReceivingAddress(new BigDecimal("2"),5,  address),
                new ReceivingAddress(new BigDecimal("2.5"),4,  address),
                new ReceivingAddress(new BigDecimal("3"),3,  address),
                new ReceivingAddress(new BigDecimal("4"),2,  address),
                new ReceivingAddress(new BigDecimal("7"),2,  address)
        );
        String expectedBalance = addresses.stream()
                .map(ReceivingAddress::getBigDecimalBalance)
                .reduce(BigDecimal::add)
                .map(balance -> bigDecimalStringConverter.toString(balance))
                .get();
        System.out.println(expectedBalance);
        int expectedConfirmations = addresses.stream()
                .mapToInt(ReceivingAddress::getConfirmations)
                .min()
                .getAsInt();

        addresses.forEach(receivingAddress -> {
            bitcoindRpcClient.sendToAddress(receivingAddress.getAddress(), receivingAddress.getBigDecimalBalance());
            String nodeAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(receivingAddress.getConfirmations(), nodeAddress);
        });
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return !tableView.getItems().isEmpty() && tableView.getItems().get(0).getBalance().equals(
                    expectedBalance + "0000000"
            );
        });

        TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, containsRow(
                address,
                expectedBalance + "0000000",
                expectedConfirmations
            )
        );
        MatcherAssert.assertThat(tableView, hasNumRows(1));

        String seed = seedGenerator.generateSeed(mnemonicSeed,"");
        String expectedNextAddress = addressSequentialGenerator
                .deriveAddresses(1, seed, FIRST_BIP84_ADDRESS_PATH.next(1))
                .get(0);
        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedNextAddress, nextAddress);
    }

    private void receiveNSequentialTransactions(FxRobot robot, String mnemonicSeed, int numberOfTransactions) throws TimeoutException {
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });

        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        IntStream.range(0, numberOfTransactions).forEach(i -> {
            bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE);
        });
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return !tableView.getItems().isEmpty() && tableView.getItems().get(0).getBalance().equals(
                    numberOfTransactions + ".00000000"
            );
        });

        TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, containsRow(
                address,
                numberOfTransactions + ".00000000",
                0
            )
        );
        MatcherAssert.assertThat(tableView, hasNumRows(1));

        String seed = seedGenerator.generateSeed(mnemonicSeed,"");
        String expectedNextAddress = addressSequentialGenerator
                .deriveAddresses(1, seed, FIRST_BIP84_ADDRESS_PATH.next(1))
                .get(0);
        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedNextAddress, nextAddress);
    }

    //TODO: diferentes numeros de confirmacao
    //TODO: fazer testes com transacoes para um endereco cujos proximos estejam usados
    //TODO: calls do teste fodem o rescan, acho

    private void receiveNTransactions(FxRobot robot, String mnemonicSeed, int numberOfTransactions) throws TimeoutException {
        try {
            WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
                TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
                String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
                return !address.isBlank();
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        IntStream.range(0, numberOfTransactions).forEach(i -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE);
            try {
                WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> {
                    TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
                    return tableView.getItems().size() == i + 1;
                });
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            MatcherAssert.assertThat(tableView, containsRow(
                    address,
                    "1.00000000",
                    0
                )
            );
            MatcherAssert.assertThat(tableView, hasNumRows(i + 1));

            String seed = seedGenerator.generateSeed(mnemonicSeed,"");
            String expectedNextAddress = addressSequentialGenerator
                    .deriveAddresses(1, seed, FIRST_BIP84_ADDRESS_PATH.next(i + 1))
                    .get(0);
            String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            assertEquals(expectedNextAddress, nextAddress);
        });
    }

    private String createWallet(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write(RandomString.make());
        robot.clickOn("#create");
        String mnemonicSeed = robot.lookup("#mnemonicSeed").queryAs(TextArea.class).getText();
        robot.clickOn("OK");
        robot.clickOn("Receive");
        return mnemonicSeed;
    }
}
