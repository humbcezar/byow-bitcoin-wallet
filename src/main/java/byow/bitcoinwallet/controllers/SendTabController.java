package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.Transaction;
import byow.bitcoinwallet.services.SendTransactionService;
import byow.bitcoinwallet.services.TaskConfigurer;
import byow.bitcoinwallet.services.TotalBalanceCalculator;
import byow.bitcoinwallet.services.TransactionCreator;
import byow.bitcoinwallet.tasks.SendTransactionTask;
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
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

@Lazy
@Component
public class SendTabController extends Tab implements BaseController {
    @FXML
    private TextField addressToSend;

    @FXML
    private TextField amountToSend;

    private Resource fxml;

    private ApplicationContext context;

    private MainController mainController;

    private Resource sendTransactionDialog;

    private SendTransactionService sendTransactionService;

    private TaskConfigurer taskConfigurer;

    private ReentrantLock reentrantLock;

    private TotalBalanceCalculator totalBalanceCalculator;

    private SendTransactionDialogController sendTransactionDialogController;

    private TransactionCreator transactionCreator;

    @Autowired
    public SendTabController(
        @Value("classpath:/fxml/send_tab.fxml") Resource fxml,
        @Value("fxml/send_transaction_dialog.fxml") Resource sendTransactionDialog,
        ApplicationContext context,
        MainController mainController,
        SendTransactionService sendTransactionService,
        TaskConfigurer taskConfigurer, ReentrantLock reentrantLock,
        TotalBalanceCalculator totalBalanceCalculator,
        SendTransactionDialogController sendTransactionDialogController,
        TransactionCreator transactionCreator
    ) throws IOException {
        this.fxml = fxml;
        this.sendTransactionDialog = sendTransactionDialog;
        this.context = context;
        this.mainController = mainController;
        this.sendTransactionService = sendTransactionService;
        this.taskConfigurer = taskConfigurer;
        this.reentrantLock = reentrantLock;
        this.totalBalanceCalculator = totalBalanceCalculator;
        this.sendTransactionDialogController = sendTransactionDialogController;
        this.transactionCreator = transactionCreator;
        setText("Send");
        construct(this.fxml, this.context);
    }

    public void initialize() {
        amountToSend.setTextFormatter(new TextFormatter<String>(digitsFilter));
    }

    public void send() throws IOException {
        Transaction transaction = transactionCreator.create(
            addressToSend.getText(),
            new BigDecimal(amountToSend.getText())
        );
        if(validateFunds(transaction)) {
            showSendTransactionDialog(transaction);
            return;
        }
        addressToSend.setText("");
        amountToSend.setText("");
    }

    private void showSendTransactionDialog(Transaction transaction) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        mainController.initializeFxml(dialog, fxmlLoader, sendTransactionDialog.getURL());
        dialog.setTitle("Send transaction");
        dialog.getDialogPane().getButtonTypes().add(OK);
        dialog.getDialogPane().getButtonTypes().add(CANCEL);
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

    private boolean validateFunds(Transaction transaction) {
        if (isNull(transaction)) {
            Alert alert = new Alert(ERROR);
            alert.setTitle("Error");
            alert.setContentText("Not enough funds available for transaction.");
            alert.show();
            return false;
        }
        return true;
    }

    private Task<Void> buildTask(Transaction transaction) {
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
                Alert alert = new Alert(ERROR);
                alert.setTitle("Error");
                alert.setContentText("Unable to send the transaction: the transaction has an output lower than the dust limit.");
                alert.show();
            }
        });
        return task;
    }

    UnaryOperator<Change> digitsFilter = change -> {
        if (change.getText().matches("([0-9]|\\.)*")) {
            return change;
        }
        return null;
    };
}
