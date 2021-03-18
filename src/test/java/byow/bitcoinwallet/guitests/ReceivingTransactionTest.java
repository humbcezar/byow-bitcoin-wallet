package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.AddressPath;
import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.TransactionRow;
import byow.bitcoinwallet.services.address.*;
import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.address.DerivationPath.*;
import static java.math.RoundingMode.UNNECESSARY;
import static java.util.stream.IntStream.range;
import static org.assertj.core.internal.bytebuddy.utility.RandomString.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.*;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class ReceivingTransactionTest extends TestBase {

    public static final int TIMEOUT = 60;
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private AddressSequentialGenerator addressSequentialGenerator;

    @Autowired
    @Qualifier("nestedSegwitAddressSequentialGenerator")
    private AddressSequentialGenerator nestedSegwitAddressSequentialGenerator;

    @Autowired
    private DefaultAddressGenerator defaultAddressGenerator;

    @Autowired
    private NextChangeAddress nextChangeAddress;

    @Autowired
    private NestedSegwitAddressGenerator nestedSegwitAddressGenerator;

    @Autowired
    private WalletUtil walletUtil;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void receiveOneTransaction(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, make(), "");
        receiveNTransactions(
            robot,
            mnemonicSeed,
            1,
            FIRST_BIP84_ADDRESS_PATH,
            defaultAddressGenerator,
            "#receivingAddress",
            addressSequentialGenerator,
            ""
        );
    }

    @Test
    public void receiveSixTransactions(FxRobot robot) throws TimeoutException {
        String password = make();
        String mnemonicSeed = walletUtil.createWallet(robot, make(), password);
        receiveNTransactions(robot, mnemonicSeed, 6, FIRST_BIP84_ADDRESS_PATH, defaultAddressGenerator, "#receivingAddress", addressSequentialGenerator, password);
    }

    @Test
    public void receiveSixSequentialTransactionsToTheSameAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, make(), "");
        int numberOfTransactions = 6;
        receiveNSequentialTransactions(robot, mnemonicSeed, numberOfTransactions);
    }

    @Test
    public void receiveFiveSequentialTransactionsToTheSameAddressWithDifferentValuesAndConfirmations(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, make(), "");

        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();

        List<ReceivingAddress> addresses = List.of(
            new ReceivingAddress(new BigDecimal("2.00000000"), 5, address),
            new ReceivingAddress(new BigDecimal("2.50000000"), 4, address),
            new ReceivingAddress(new BigDecimal("3.00000000"), 3, address),
            new ReceivingAddress(new BigDecimal("4.00000000"), 2, address),
            new ReceivingAddress(new BigDecimal("7.00000000"), 2, address)
        );
        String expectedBalance = addresses.stream()
            .map(ReceivingAddress::getBigDecimalBalance)
            .reduce(BigDecimal::add)
            .get()
            .toString();
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String expectedNextAddress = addressSequentialGenerator
                .deriveAddresses(1, seed, FIRST_BIP84_ADDRESS_PATH.next(1))
                .get(0).getAddress();
        receiveVaryingTransactions(robot, address, addresses, expectedNextAddress, expectedBalance);
    }

    @Test
    public void receiveTransactionNextAddressUsed(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, make(), "");

        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();

        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        List<String> expectedAddresses = addressSequentialGenerator.deriveAddresses(
                2,
                seed,
                FIRST_BIP84_ADDRESS_PATH.next(1)
        ).stream().map(AddressPath::getAddress).collect(Collectors.toList());
        String secondAddress = expectedAddresses.get(0);
        String expectedNextAddress = expectedAddresses.get(1);
        List<ReceivingAddress> addresses = List.of(
                new ReceivingAddress(new BigDecimal("2.00000000"), 0, secondAddress),
                new ReceivingAddress(new BigDecimal("2.50000000"), 0, address)
        );
        String expectedBalance = "2.50000000";
        receiveVaryingTransactions(robot, address, addresses, expectedNextAddress, expectedBalance);
    }

    @Test
    public void receiveSixTransactionToNestedSegwitAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, make(), "");
        receiveNTransactions(
                robot,
                mnemonicSeed,
                5,
                FIRST_BIP49_ADDRESS_PATH,
                nestedSegwitAddressGenerator,
                "#nestedReceivingAddress",
                nestedSegwitAddressSequentialGenerator,
            "");
    }

    @Test
    public void receiveSixTransactionToChangeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, make(), "");
        receiveNTransactions(
            robot,
            mnemonicSeed,
            5,
            FIRST_BIP84_CHANGE_PATH,
            defaultAddressGenerator,
            addressSequentialGenerator
        );
    }

    @Test
    public void receiveOneHashBlockRewardWalletAddress(FxRobot robot) throws TimeoutException, InterruptedException {
        walletUtil.createWallet(robot, RandomString.make(), "");
        waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        robot.sleep(5000);

        bitcoindRpcClient.generateToAddress(1, address);

        String randomAddress = addressGenerator.generate(
            seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), ""),
            FIRST_BIP84_ADDRESS_PATH
        );
        bitcoindRpcClient.generateToAddress(100, randomAddress);

        waitFor(40, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1;
        });
        TableView<ReceivingAddress> table = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(table.getItems().get(0).getAddress(), address);

        waitFor(40, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().get(0).getConfirmations() == 101;
        });
        TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(101, tableView.getItems().get(0).getConfirmations());
    }

    private void receiveNSequentialTransactions(FxRobot robot, String mnemonicSeed, int numberOfTransactions) throws TimeoutException {
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });

        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        range(0, numberOfTransactions).forEach(i -> {
            bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE);
        });
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return !tableView.getItems().isEmpty() && tableView.getItems().get(0).getBalance().equals(
                    numberOfTransactions + ".00000000"
            );
        });

        TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertThat(tableView, containsRow(
                address,
                numberOfTransactions + ".00000000",
                0
                )
        );
        assertThat(tableView, hasNumRows(1));

        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String expectedNextAddress = addressSequentialGenerator
                .deriveAddresses(1, seed, FIRST_BIP84_ADDRESS_PATH.next(1))
                .get(0).getAddress();
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return expectedNextAddress.equals(nextAddress);
        });

        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedNextAddress, nextAddress);
    }

    private void receiveNTransactions(
            FxRobot robot,
            String mnemonicSeed,
            int numberOfTransactions,
            DerivationPath firstDerivationPath,
            AddressGenerator addressGenerator,
            AddressSequentialGenerator addressSequentialGenerator
    ) throws TimeoutException {
        robot.clickOn("#addressesTab");
        String firstAddress = addressGenerator.generate(seedGenerator.generateSeed(mnemonicSeed, ""), firstDerivationPath);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String address = nextChangeAddress.getValue().getAddress();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        List<String> txIds = new ArrayList<>();
        range(0, numberOfTransactions).forEach(i -> {
            String address = nextChangeAddress.getValue().getAddress();
            txIds.add(bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));
            try {
                waitFor(60, TimeUnit.SECONDS, () -> {
                    TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
                    return tableView.getItems().size() == i + 1;
                });
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            assertThat(tableView, hasNumRows(i + 1));
            assertThat(tableView, containsRow(
                    address,
                    "1.00000000",
                    0
                )
            );

            String seed = seedGenerator.generateSeed(mnemonicSeed, "");
            nextReceivingAddressAssertion(firstDerivationPath, addressSequentialGenerator, i, seed);
        });

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> numberOfTransactions == transactionsTable.getItems().size());
        assertEquals(numberOfTransactions, transactionsTable.getItems().size());
        Map<String, TransactionRow> trRowMap = new HashMap<>();
        transactionsTable.getItems().stream().forEach(transactionRow -> trRowMap.put(transactionRow.getTransactionId(), transactionRow));
        assertEquals(numberOfTransactions, transactionsTable.getItems().size());
        range(0, txIds.size()).forEach(i ->
            assertThat(transactionsTable, containsRow(
                txIds.get(i),
                "1.00000000",
                0,
                trRowMap.get(txIds.get(i)).getDate()
            ))
        );
    }

    private void nextReceivingAddressAssertion(DerivationPath firstDerivationPath, AddressSequentialGenerator addressSequentialGenerator, int i, String seed) {
        String expectedNextAddress = addressSequentialGenerator
                .deriveAddresses(1, seed, firstDerivationPath.next(i + 1))
                .get(0).getAddress();
        try {
            waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
                String nextAddress = nextChangeAddress.getValue().getAddress();
                return expectedNextAddress.equals(nextAddress);
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        String nextAddress = nextChangeAddress.getValue().getAddress();
        assertEquals(expectedNextAddress, nextAddress);
    }

    private void receiveNTransactions(
        FxRobot robot,
        String mnemonicSeed,
        int numberOfTransactions,
        DerivationPath firstDerivationPath,
        AddressGenerator addressGenerator,
        String receivingAddressQuery,
        AddressSequentialGenerator addressSequentialGenerator,
        String password
    ) throws TimeoutException {
        robot.clickOn("#addressesTab");
        String firstAddress = addressGenerator.generate(seedGenerator.generateSeed(mnemonicSeed, password), firstDerivationPath);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            robot.lookup("#addressesTable").queryAs(TableView.class);
            String address = robot.lookup(receivingAddressQuery).queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        List<String> txIds = new ArrayList<>();
        range(0, numberOfTransactions).forEach(i -> {
            String address = robot.lookup(receivingAddressQuery).queryAs(TextField.class).getText();
            txIds.add(bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE));
            try {
                waitFor(60, TimeUnit.SECONDS, () -> {
                    TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
                    return tableView.getItems().size() == i + 1;
                });
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            assertThat(tableView, hasNumRows(i + 1));
            assertThat(tableView, containsRow(
                    address,
                    "1.00000000",
                    0
                )
            );

            String seed = seedGenerator.generateSeed(mnemonicSeed, password);
            String expectedNextAddress = addressSequentialGenerator
                    .deriveAddresses(1, seed, firstDerivationPath.next(i + 1))
                    .get(0).getAddress();
            try {
                waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
                    String nextAddress = robot.lookup(receivingAddressQuery).queryAs(TextField.class).getText();
                    return expectedNextAddress.equals(nextAddress);
                });
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            String nextAddress = robot.lookup(receivingAddressQuery).queryAs(TextField.class).getText();
            assertEquals(expectedNextAddress, nextAddress);
        });

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        Map<String, TransactionRow> trRowMap = new HashMap<>();
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> numberOfTransactions == transactionsTable.getItems().size());
        transactionsTable.getItems().stream().forEach(transactionRow -> trRowMap.put(transactionRow.getTransactionId(), transactionRow));
        assertEquals(numberOfTransactions, transactionsTable.getItems().size());
        range(0, txIds.size()).forEach(i ->
            assertThat(transactionsTable, containsRow(
                txIds.get(i),
                "1.00000000",
                0,
                trRowMap.get(txIds.get(i)).getDate()
            ))
        );
    }

    private void receiveVaryingTransactions(
            FxRobot robot,
            String address,
            List<ReceivingAddress> addresses,
            String expectedNextAddress,
            String expectedBalance
    ) throws TimeoutException {
        robot.clickOn("#addressesTab");
        int expectedConfirmations = addresses.stream()
                .mapToInt(ReceivingAddress::getConfirmations)
                .min()
                .getAsInt();

        List<String> txIds = addresses.stream().map(receivingAddress -> {
            String txId = bitcoindRpcClient.sendToAddress(receivingAddress.getAddress(), receivingAddress.getBigDecimalBalance());
            String nodeAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(receivingAddress.getConfirmations(), nodeAddress);
            return txId;
        }).collect(Collectors.toList());
        int expectedNumberOfRows = (int) addresses.stream().map(ReceivingAddress::getAddress).distinct().count();
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return !tableView.getItems().isEmpty() && tableView.getItems().get(0).getBalance().equals(expectedBalance)
                    && tableView.getItems().get(0).getConfirmations() == expectedConfirmations
                    && tableView.getItems().size() == expectedNumberOfRows;
        });

        TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertThat(tableView, containsRow(
                address,
                expectedBalance,
                expectedConfirmations
                )
        );
        assertThat(tableView, hasNumRows(expectedNumberOfRows));

        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return expectedNextAddress.equals(nextAddress);
        });
        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedNextAddress, nextAddress);

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> txIds.size() == transactionsTable.getItems().size());
        assertThat(transactionsTable, hasNumRows(txIds.size()));
        List<Integer> confirmationsForTransactions = addresses.stream()
            .mapToInt(ReceivingAddress::getConfirmations)
            .boxed()
            .collect(Collectors.toList());
        Stack<Integer> expectedConfirmationsForTransactions = new Stack<>();
        for (int i = confirmationsForTransactions.size() - 1; i >= 0; i--) {
            int numToAdd = expectedConfirmationsForTransactions.empty() ? 0 : expectedConfirmationsForTransactions.peek();
            expectedConfirmationsForTransactions.push(numToAdd + confirmationsForTransactions.get(i));
        }
        Map<String, TransactionRow> trRowMap = new HashMap<>();
        transactionsTable.getItems().stream().forEach(transactionRow -> trRowMap.put(transactionRow.getTransactionId(), transactionRow));
        range(0, txIds.size()).forEach(i ->
            assertThat(transactionsTable, containsRow(
                txIds.get(i),
                addresses.get(i).getBigDecimalBalance().setScale(8, UNNECESSARY).toString(),
                expectedConfirmationsForTransactions.pop(),
                trRowMap.get(txIds.get(i)).getDate()
            ))
        );
    }
}
