package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.*;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.address.*;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.hamcrest.MatcherAssert;
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
import java.util.stream.IntStream;

import static byow.bitcoinwallet.services.address.DerivationPath.*;
import static java.util.stream.IntStream.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.*;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class TablesTest extends TestBase {

    public static final int TIMEOUT = 40;

    @Autowired
    private NextChangeAddress nextChangeAddress;

    @Autowired
    private NextNestedSegwitAddress nextNestedSegwitAddress;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AddressSequentialGenerator defaultAddressSequentialGenerator;

    @Autowired
    private NestedSegwitAddressGenerator nestedSegwitAddressGenerator;

    @Autowired
    @Qualifier("nestedSegwitAddressSequentialGenerator")
    private AddressSequentialGenerator nestedSegwitAddressSequentialGenerator;

    @Autowired
    private MultiAddressUpdater multiAddressUpdater;

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
        showNAddressesWithPositiveBalance(robot, 1, FIRST_BIP84_ADDRESS_PATH, defaultAddressSequentialGenerator);
        assertNextReceivingAddress(robot, 1);
        assertNextChangeAddress(0);
    }

    @Test
    public void showFiveAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 5, FIRST_BIP84_ADDRESS_PATH, defaultAddressSequentialGenerator);
        assertNextReceivingAddress(robot, 5);
        assertNextChangeAddress(0);
    }

    @Test
    public void showSixAddressWithPositiveBalance(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 6, FIRST_BIP84_ADDRESS_PATH, defaultAddressSequentialGenerator);
        assertNextReceivingAddress(robot, 6);
        assertNextChangeAddress(0);
    }

    @Test
    public void showSixAddressWithPositiveBalanceWithChangeAddresses(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 6, FIRST_BIP84_CHANGE_PATH, defaultAddressSequentialGenerator);
        assertNextChangeAddress(6);
        assertNextReceivingAddress(robot, 0);
    }

    @Test
    public void showSixAddressWithPositiveBalanceWithNestedSegwitAddresses(FxRobot robot) throws TimeoutException {
        showNAddressesWithPositiveBalance(robot, 6, FIRST_BIP49_ADDRESS_PATH, nestedSegwitAddressSequentialGenerator);
        assertNextNestedSegwitAddress(robot, 6);
        assertNextReceivingAddress(robot, 0);
        assertNextChangeAddress(0);
    }

    @Test
    public void showThreeAddressesWithPositiveValueWithVaryingTransactionsWithChangeAddresses(FxRobot robot) throws TimeoutException {
        multiAddressUpdater.setInitialAddressToMonitor(20);

        int numberOfReceivingAddresses = 3;

        List<String> addresses = defaultAddressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed, FIRST_BIP84_CHANGE_PATH
        ).stream().map(AddressPath::getAddress).collect(Collectors.toList());
        String fromAddress = bitcoindRpcClient.getNewAddress();

        List<String> txIds = new ArrayList<>();

        List<Integer> expectedConfirmations = List.of(11, 11, 10, 9, 9, 9, 8, 7, 6, 5);
        //balance:4, confirmations:11,
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(0), BigDecimal.ONE));
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(0), new BigDecimal(3)));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        //balance:10, confirmations:9,
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(1), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(1), new BigDecimal(2)));
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(1), new BigDecimal(7)));
        //balance:5, confirmations:5,
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(5, fromAddress);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == numberOfReceivingAddresses;
        });

        TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);

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

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () ->
            transactionsTable.getItems().size() == txIds.size()
        );
        Map<String, TransactionRow> trRowMap = new HashMap<>();
        transactionsTable.getItems().stream().forEach(transactionRow -> trRowMap.put(transactionRow.getTransactionId(), transactionRow));
        assertEquals(txIds.size(), transactionsTable.getItems().size());
        range(0, txIds.size()).forEach(i -> {
            switch (i) {
                case 1 -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "3.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
                case 3 -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "2.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
                case 4 -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "7.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
                default -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "1.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
            }
        });
    }

    @Test
    public void showThreeAddressesWithPositiveValueWithVaryingTransactions(FxRobot robot) throws TimeoutException {
        multiAddressUpdater.setInitialAddressToMonitor(20);

        int numberOfReceivingAddresses = 3;

        List<String> addresses = defaultAddressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed, FIRST_BIP84_ADDRESS_PATH
        ).stream().map(AddressPath::getAddress).collect(Collectors.toList());
        String fromAddress = bitcoindRpcClient.getNewAddress();
        List<String> txIds = new ArrayList<>();
        List<Integer> expectedConfirmations = List.of(11, 11, 10, 9, 9, 9, 8, 7, 6, 5);

        //balance:4, confirmations:11,
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(0), BigDecimal.ONE));
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(0), new BigDecimal(3)));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        //balance:10, confirmations:9,
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(1), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(1), new BigDecimal(2)));
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(1), new BigDecimal(7)));
        //balance:5, confirmations:5,
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(1, fromAddress);
        txIds.add(bitcoindRpcClient.sendToAddress(addresses.get(2), BigDecimal.ONE));
        bitcoindRpcClient.generateToAddress(5, fromAddress);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == numberOfReceivingAddresses;
        });

        TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);

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

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () ->
            transactionsTable.getItems().size() == txIds.size()
        );
        Map<String, TransactionRow> trRowMap = new HashMap<>();
        transactionsTable.getItems().stream().forEach(transactionRow -> trRowMap.put(transactionRow.getTransactionId(), transactionRow));
        assertEquals(txIds.size(), transactionsTable.getItems().size());
        range(0, txIds.size()).forEach(i -> {
            switch (i) {
                case 1 -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "3.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
                case 3 -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "2.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
                case 4 -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "7.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
                default -> assertThat(transactionsTable, containsRow(
                    txIds.get(i),
                    "1.0",
                    expectedConfirmations.get(i),
                    trRowMap.get(txIds.get(i)).getDate()
                ));
            }
        });
    }

    private void showNAddressesWithPositiveBalance(
        FxRobot robot,
        int numberOfReceivingAddresses,
        DerivationPath firstDerivationPath,
        AddressSequentialGenerator addressSequentialGenerator
    ) throws TimeoutException {
        robot.clickOn("#addressesTab");
        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                numberOfReceivingAddresses,
                seed,
                firstDerivationPath
        ).stream().map(AddressPath::getAddress).collect(Collectors.toList());

        List<String> txIds = new ArrayList<>();
        addresses.forEach(address -> txIds.add(bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE)));

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(walletName);
        robot.clickOn("Receive");
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == numberOfReceivingAddresses;
        });

        TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);

        IntStream.range(0, numberOfReceivingAddresses).boxed().forEach(i -> {
            MatcherAssert.assertThat(tableView, containsRow(
                    addresses.get(i),
                    "1.00000000",
                    0
                )
            );
        });
        MatcherAssert.assertThat(tableView, hasNumRows(numberOfReceivingAddresses));

        robot.clickOn("#transactionsTab");
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        waitFor(TIMEOUT, TimeUnit.SECONDS, () ->
            transactionsTable.getItems().size() == txIds.size()
        );
        Map<String, TransactionRow> trRowMap = new HashMap<>();
        transactionsTable.getItems().stream().forEach(transactionRow -> trRowMap.put(transactionRow.getTransactionId(), transactionRow));
        assertEquals(txIds.size(), transactionsTable.getItems().size());
        range(0, txIds.size()).forEach(i ->
            assertThat(transactionsTable, containsRow(
                txIds.get(i),
                "1.0",
                0,
                trRowMap.get(txIds.get(i)).getDate()
            ))
        );
    }

    private void assertNextReceivingAddress(FxRobot robot, int numberOfReceivingAddresses) throws TimeoutException {
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/0/" + numberOfReceivingAddresses));
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && address.equals(expectedReceivingAddress);
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedReceivingAddress, address);
    }

    private void assertNextChangeAddress(int numberOfReceivingAddresses) throws TimeoutException {
        String expectedReceivingAddress = addressGenerator.generate(seed, new DerivationPath("84'/0'/0'/1/" + numberOfReceivingAddresses));
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String address =  nextChangeAddress.getValue().getAddress();
            return address != null && address.equals(expectedReceivingAddress);
        });
        String address =  nextChangeAddress.getValue().getAddress();
        assertEquals(expectedReceivingAddress, address);
    }

    private void assertNextNestedSegwitAddress(FxRobot robot, int numberOfReceivingAddresses) throws TimeoutException {
        String expectedReceivingAddress = nestedSegwitAddressGenerator.generate(
            seed, new DerivationPath("49'/0'/0'/0/" + numberOfReceivingAddresses)
        );
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String address =  nextNestedSegwitAddress.getValue().getAddress();
            return address != null && address.equals(expectedReceivingAddress);
        });
        String address =  nextNestedSegwitAddress.getValue().getAddress();
        assertEquals(expectedReceivingAddress, address);
        String guiAddress = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();
        assertEquals(expectedReceivingAddress, guiAddress);
    }

    //TODO: testar com floating btc recebidos
}