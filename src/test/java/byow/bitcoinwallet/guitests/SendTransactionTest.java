package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.TransactionRow;
import byow.bitcoinwallet.services.address.AddressGenerator;
import byow.bitcoinwallet.services.address.NestedSegwitAddressGenerator;
import byow.bitcoinwallet.services.address.SeedGenerator;
import byow.bitcoinwallet.services.wallet.TotalBalanceCalculator;
import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.address.DerivationPath.*;
import static java.lang.Integer.MAX_VALUE;
import static java.math.BigDecimal.*;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;
import static net.bytebuddy.utility.RandomString.make;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class SendTransactionTest extends TestBase {

    @Autowired
    private WalletUtil walletUtil;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private NestedSegwitAddressGenerator nestedSegwitAddressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private TotalBalanceCalculator totalBalanceCalculator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void sendOneTransactionToNodeAddress(FxRobot robot) throws TimeoutException {
        String password = make();
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), password);
        String seed = seedGenerator.generateSeed(mnemonicSeed, password);
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "bech32", 1, password);
    }

    @Test
    public void sendOneTransactionWithWrongPasswordFail(FxRobot robot) throws TimeoutException {
        String password = make();
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), password);
        String seed = seedGenerator.generateSeed(mnemonicSeed, password);
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.5");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");
        robot.clickOn("#sendTransactionPassword");
        robot.write("wrong");
        robot.clickOn("OK");
        NodeQuery text = robot.lookup("Wrong password.");
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }

    @Test
    public void sendOneTransactionToNodeAddressFromNestedSegwitUtxo(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = nestedSegwitAddressGenerator.generate(seed, FIRST_BIP49_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#nestedReceivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "bech32", 1, "");
    }

    @Test
    public void sendOneTransactionToNodeAddressFromNestedSegwitUtxoAndDefaultUtxo(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = nestedSegwitAddressGenerator.generate(seed, FIRST_BIP49_ADDRESS_PATH);
        String secondAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#nestedReceivingAddress");
        fundAddress(robot, secondAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 2 && tableView.getItems().get(0).getConfirmations() == 1
                    && tableView.getItems().get(1).getConfirmations() == 2;
        });

        sendNTransactions(robot, "1.5", 1, "1.50000000", 1, seed, 0, "bech32", 2, "");
    }

    @Test
    public void sendFiveTransactionsToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, valueOf(5), 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.8", 1, "0.80000000", 5, seed, 0, "bech32", 1, "");
    }

    @Test
    public void sendFiveTransactionsFromWalletWithThreeUtxosToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");

        fundNAddresses(robot, seed, valueOf(2), 1, 3);
        sendNTransactions(robot, "0.8", 1, "0.80000000", 5, 3);
    }

    @Test
    public void sendOneTransactionFromWalletWithThreeUtxosToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");

        fundNAddresses(robot, seed, valueOf(2), 1, 3);
        sendNTransactions(robot, "5", 1, "5.00000000", 1, 3);
    }

    @Test
    public void sendOneTransactionWithoutEnoughFundsFail(FxRobot robot) {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");

        fundNAddresses(robot, seed, valueOf(2), 1, 1);

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("5");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Not enough funds available for transaction.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionToAnotherByowWalletThenSpendIt(FxRobot robot) throws TimeoutException {
        String recipientWallet = RandomString.make();
        String recipientMnemonicSeed = walletUtil.createWallet(robot, recipientWallet, "");
        String recipientSeed = seedGenerator.generateSeed(recipientMnemonicSeed, "");
        String recipientWalletAddress = addressGenerator.generate(recipientSeed, FIRST_BIP84_ADDRESS_PATH);
        waitFor(60, SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return address != null && address.equals(recipientWalletAddress);
        });

        String senderMnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(senderMnemonicSeed, "");
        String senderWalletAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, senderWalletAddress, ONE, 1, "#receivingAddress");

        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.5");
        robot.clickOn("#addressToSend");
        robot.write(recipientWalletAddress);
        robot.clickOn("#send");
        robot.clickOn("OK");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 &&
                tableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) < 0;
        });
        TableView<ReceivingAddress> senderTableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), senderTableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(1, nodeAddress);

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(recipientWallet);
        robot.clickOn("OK");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> recipientTableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            ProgressBar progressBar = robot.lookup("#progressBar").queryAs(ProgressBar.class);
            return recipientTableView.getItems().size() == 1 &&
                recipientTableView.getItems().get(0).getBigDecimalBalance().compareTo(new BigDecimal("0.5")) == 0 &&
                !progressBar.isIndeterminate();
        });
        TableView<ReceivingAddress> recipientTableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        assertEquals(new BigDecimal("0.5"), recipientTableView.getItems().get(0).getBigDecimalBalance().setScale(1, HALF_UP));

        sendNTransactions(robot, "0.25", 2, "0.25000000", 1, recipientSeed, 0, "bech32", 1, "");
        //TODO: esquematizar novamente esquema de associar tx com wallet, pois qdo wallet recebida eh a msm, da pau nesse teste (input como output, por ex)
        //TODO: arrumar waitfor comparativo (0.5 < 0)
        //TODO: ver se qdo voltar para wallet anterior, tx nao vai estar cagada
    }

    @Test
    public void sendOneTransactionWithDustOutputToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.00000293");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Unable to send the transaction: the transaction has an output lower than the dust limit.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionWithDustChangeToNodeAddressWithInsufficientFundsForChangeFail(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("0.99999999");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Insufficient funds for calculated fee.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionWithDustChangeToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            BigDecimal previousBalance = totalBalanceCalculator.getTotalBalance();
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1
                && previousBalance.equals(new BigDecimal("1.00000000"));
        });

        //1 - totalFee(which is 2679) - 200 sats -> generating dust change
        sendNTransactions(robot, "0.99997121", 4, "0.99997121", 1, 1);
    }

    @Test
    public void sendOneTransactionWithInputsEqualAdjustedTargetToNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            BigDecimal previousBalance = totalBalanceCalculator.getTotalBalance();
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1
                && previousBalance.equals(new BigDecimal("1.00000000"));
        });

        //1 - totalFee(which is 2679)
        sendNTransactions(robot, "0.99997321", 4, "0.99997321", 1, 1);
    }

    @Test
    public void sendOneTransactionWithInputsEqualTargetToNodeAddressFail(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("1");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Insufficient funds for calculated fee.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionWithTotalFeeGreaterThanDustButLesserThanIntendedFee(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, valueOf(1.00000300), 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        String nodeAddress = bitcoindRpcClient.getNewAddress();
        robot.clickOn("#sendTab");
        robot.clickOn("#amountToSend");
        robot.write("1");
        robot.clickOn("#addressToSend");
        robot.write(nodeAddress);
        robot.clickOn("#send");

        NodeQuery text = robot.lookup("Insufficient funds for calculated fee.");
        assertNotNull(text.queryLabeled().getText());
        robot.clickOn("OK");
    }

    @Test
    public void sendOneTransactionToNestedSegwitNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "p2sh-segwit", 1, "");
    }

    @Test
    public void sendOneTransactionToLegacyNodeAddress(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = walletUtil.createWallet(robot, RandomString.make(), "");
        String seed = seedGenerator.generateSeed(mnemonicSeed, "");
        String firstAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        fundAddress(robot, firstAddress, ONE, 1, "#receivingAddress");
        waitFor(60, SECONDS, () -> {
            TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1 && tableView.getItems().get(0).getConfirmations() == 1;
        });

        sendNTransactions(robot, "0.5", 1, "0.50000000", 1, seed, 0, "legacy", 1, "");
    }

    private void fundAddress(FxRobot robot, String firstAddress, BigDecimal amount, int confirmations, String fxmlAddress) throws TimeoutException {
        robot.clickOn("#addressesTab");
        waitFor(40, SECONDS, () -> {
            robot.lookup("#addressesTable").queryAs(TableView.class);
            String address = robot.lookup(fxmlAddress).queryAs(TextField.class).getText();
            return address != null && !address.isBlank() && address.equals(firstAddress);
        });
        String address = robot.lookup(fxmlAddress).queryAs(TextField.class).getText();
        bitcoindRpcClient.sendToAddress(address, amount);
        String nodeAddress = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(confirmations, nodeAddress);
    }

    private void fundNAddresses(
        FxRobot robot,
        String seed,
        BigDecimal amount,
        int confirmations,
        int numberOfAddresses
    ) {
        robot.clickOn("#addressesTab");
        range(0, numberOfAddresses).forEach(i -> {
            String address = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(i));
            try {
                fundAddress(robot, address, amount, confirmations, "#receivingAddress");
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            waitFor(60, SECONDS, () -> {
                TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
                return tableView.getItems().size() == numberOfAddresses
                        && tableView.getItems().get(0).getConfirmations() == numberOfAddresses;
            });
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNTransactions(
        FxRobot robot,
        String amount,
        int scale,
        String expectedBalance,
        int numTransactions,
        String seed,
        int firstChangeIndex,
        String toAddressType,
        int numPreviousTransactions,
        String password
    ) throws TimeoutException {
        List<String> transactionAmountsSent = range(0, numTransactions).mapToObj(i -> {
            TableView<ReceivingAddress> table = robot.lookup("#addressesTable").queryAs(TableView.class);
            BigDecimal previousBalance = table.getItems().stream().map(ReceivingAddress::getBigDecimalBalance).reduce(BigDecimal::add).orElse(ZERO);

            String nodeAddress = bitcoindRpcClient.getNewAddress("", toAddressType);
            robot.clickOn("#sendTab");
            robot.clickOn("#amountToSend");
            robot.write(amount);
            robot.clickOn("#addressToSend");
            robot.write(nodeAddress);
            robot.clickOn("#send");
            robot.clickOn("#sendTransactionPassword");
            robot.write(password);
            robot.clickOn("OK");
            try {
                waitFor(60, SECONDS, () -> {
                    TableView<ReceivingAddress> tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
                    return !isNull(tableView) && tableView.getItems().size() == 1
                            && previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP).equals(
                                tableView.getItems().get(0).getBigDecimalBalance().setScale(scale, HALF_UP)
                        );
                });
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            assertEquals(
                previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP),
                table.getItems().get(0).getBigDecimalBalance().setScale(scale, HALF_UP)
            );

            assertEquals(
                addressGenerator.generate(seed, FIRST_BIP84_CHANGE_PATH.next(i + firstChangeIndex)),
                table.getItems().get(0).getAddress()
            );

            String nodeConfirmationAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(1, nodeConfirmationAddress);

            BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
            assertEquals(new BigDecimal(expectedBalance), nodeAddressBalance);
            return previousBalance.subtract(table.getItems().get(0).getBigDecimalBalance()).toString();
        }).collect(Collectors.toList());

        robot.clickOn("#transactionsTab");
        waitFor(60, SECONDS, () -> {
            TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
            return transactionsTable.getItems().size() == (numTransactions + numPreviousTransactions);
        });
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        assertThat(transactionsTable, hasNumRows(numTransactions + numPreviousTransactions));
        range(0, numTransactions).forEach(i ->
            assertEquals(
                "-".concat(transactionAmountsSent.get(i)),
                transactionsTable.getItems().get(i + numPreviousTransactions).getBigDecimalBalance().setScale(8, FLOOR).toString()
            )
        );
    }

    private void sendNTransactions(
        FxRobot robot,
        String amount,
        int scale,
        String expectedBalance,
        int numberOfTransactions,
        int numberOfPreviousTransactions
    ) throws TimeoutException {
        List<String> transactionAmountsSent = range(0, numberOfTransactions).mapToObj(i -> {
            BigDecimal previousBalance = totalBalanceCalculator.getTotalBalance();

            String nodeAddress = bitcoindRpcClient.getNewAddress();
            robot.clickOn("#sendTab");
            robot.clickOn("#amountToSend");
            robot.write(amount);
            robot.clickOn("#addressToSend");
            robot.write(nodeAddress);
            robot.clickOn("#send");
            robot.clickOn("OK");
            try {
                waitFor(60, SECONDS, () ->
                    previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP)
                        .equals(totalBalanceCalculator.getTotalBalance().setScale(scale, HALF_UP))
                );
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
            assertEquals(
                previousBalance.subtract(new BigDecimal(amount)).setScale(scale, HALF_UP),
                totalBalanceCalculator.getTotalBalance().setScale(scale, HALF_UP)
            );

            String nodeConfirmationAddress = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(1, nodeConfirmationAddress);

            BigDecimal nodeAddressBalance = bitcoindRpcClient.listUnspent(0, MAX_VALUE, nodeAddress).get(0).amount();
            assertEquals(new BigDecimal(expectedBalance), nodeAddressBalance);

            return previousBalance.subtract(totalBalanceCalculator.getTotalBalance()).toString();
        }).collect(Collectors.toList());

        robot.clickOn("#transactionsTab");
        waitFor(60, SECONDS, () -> {
            TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
            return transactionsTable.getItems().size() == (numberOfTransactions + numberOfPreviousTransactions);
        });
        TableView<TransactionRow> transactionsTable = robot.lookup("#transactionsTable").queryAs(TableView.class);
        assertThat(transactionsTable, hasNumRows(numberOfTransactions + numberOfPreviousTransactions));
        range(0, numberOfTransactions).forEach(i ->
            assertEquals(
                "-".concat(transactionAmountsSent.get(i)),
                transactionsTable.getItems().get(i + numberOfPreviousTransactions).getBigDecimalBalance().setScale(8, FLOOR).toString()
            )
        );
    }
}
