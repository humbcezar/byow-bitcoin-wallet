package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.services.gui.DialogService;
import byow.bitcoinwallet.services.transaction.DustCalculator;
import byow.bitcoinwallet.services.transaction.SendTransactionService;
import byow.bitcoinwallet.services.transaction.TransactionCreator;
import byow.bitcoinwallet.tasks.SendTransactionTask;
import byow.bitcoinwallet.tasks.TaskConfigurer;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_FAILED;
import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonType.OK;

@Lazy
@Component
public class SendTabController extends Tab implements BaseController {
    @FXML
    private TextField addressToSend;

    @FXML
    private TextField amountToSend;

    private final Resource fxml;

    private final ApplicationContext context;

    private final Resource sendTransactionDialog;

    private final SendTransactionService sendTransactionService;

    private final TaskConfigurer taskConfigurer;

    private final ReentrantLock reentrantLock;

    private final SendTransactionDialogController sendTransactionDialogController;

    private final TransactionCreator transactionCreator;

    private final DustCalculator dustCalculator;

    private final DialogService dialogService;

    @Autowired
    public SendTabController(
        @Value("classpath:/fxml/send_tab.fxml") Resource fxml,
        @Value("fxml/send_transaction_dialog.fxml") Resource sendTransactionDialog,
        ApplicationContext context,
        SendTransactionService sendTransactionService,
        TaskConfigurer taskConfigurer, ReentrantLock reentrantLock,
        SendTransactionDialogController sendTransactionDialogController,
        TransactionCreator transactionCreator,
        DustCalculator dustCalculator,
        DialogService dialogService
    ) throws IOException {
        this.fxml = fxml;
        this.sendTransactionDialog = sendTransactionDialog;
        this.context = context;
        this.sendTransactionService = sendTransactionService;
        this.taskConfigurer = taskConfigurer;
        this.reentrantLock = reentrantLock;
        this.sendTransactionDialogController = sendTransactionDialogController;
        this.transactionCreator = transactionCreator;
        this.dustCalculator = dustCalculator;
        this.dialogService = dialogService;
        setText("Send");
        construct(this.fxml, this.context);
    }

    public void initialize() {
        amountToSend.setTextFormatter(new TextFormatter<String>(digitsFilter));
    }

    public void send() throws IOException {
        BigDecimal amount = new BigDecimal(amountToSend.getText());
        if (dustCalculator.isDust(amount)) {
            showDustAlert();
            addressToSend.setText("");
            amountToSend.setText("");
            return;
        }
        WallyTransaction transaction = transactionCreator.create(
            addressToSend.getText(),
            amount
        );
        if(validateFunds(transaction) && validateTotalFee(transaction)) {
            showSendTransactionDialog(transaction);
            return;
        }
        addressToSend.setText("");
        amountToSend.setText("");
    }

    private void showSendTransactionDialog(WallyTransaction transaction) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        dialogService.initialize(dialog, fxmlLoader, sendTransactionDialog.getURL(), "Send transaction");
        sendTransactionDialogController.buildTransactionInformation(
            amountToSend.getText(),
            addressToSend.getText(),
            transaction
        );
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == OK) {
            new Thread(buildTask(transaction)).start();
        }
    }

    private boolean validateFunds(WallyTransaction transaction) {
        if (isNull(transaction)) {
            Alert alert = new Alert(ERROR);
            alert.setTitle("Error");
            alert.setContentText("Not enough funds available for transaction.");
            alert.show();
            return false;
        }
        return true;
    }

    private boolean validateTotalFee(WallyTransaction transaction) {
        if(transaction.getTotalFeeInSatoshis() < transaction.getIntendedTotalFeeInSatoshis()) {
            Alert alert = new Alert(ERROR);
            alert.setTitle("Error");
            alert.setContentText("Insufficient funds for calculated fee.");
            alert.show();
            return false;
        }
        return true;
    }

    private Task<Void> buildTask(WallyTransaction transaction) {
        Task<Void> task = taskConfigurer.configure(
            new SendTransactionTask(
                reentrantLock,
                sendTransactionService,
                transaction
            ),
            "Sending transaction..."
        );
        task.addEventFilter(WORKER_STATE_SUCCEEDED, event -> {
            amountToSend.setText("");
            addressToSend.setText("");
        });
        task.addEventFilter(WORKER_STATE_FAILED, event -> {
            amountToSend.setText("");
            addressToSend.setText("");
            BitcoinRPCException exception = (BitcoinRPCException) event.getSource().getException();
            if (exception.getRPCError().getMessage().equals("dust")) {
                showDustAlert();
            }
        });
        return task;
    }

    private void showDustAlert() {
        Alert alert = new Alert(ERROR);
        alert.setTitle("Error");
        alert.setContentText("Unable to send the transaction: the transaction has an output lower than the dust limit.");
        alert.show();
    }

    UnaryOperator<Change> digitsFilter = change -> {
        if (change.getText().matches("([0-9]|\\.)*")) {
            return change;
        }
        return null;
    };
}
