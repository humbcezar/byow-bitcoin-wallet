package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.SendTransactionService;
import byow.bitcoinwallet.services.TaskConfigurer;
import byow.bitcoinwallet.tasks.SendTransactionTask;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SCHEDULED;

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

    @Autowired
    public SendTabController(
        @Value("classpath:/fxml/send_tab.fxml") Resource fxml,
        @Value("fxml/send_transaction_dialog.fxml") Resource sendTransactionDialog,
        ApplicationContext context,
        MainController mainController,
        SendTransactionService sendTransactionService,
        TaskConfigurer taskConfigurer, ReentrantLock reentrantLock
    ) throws IOException {
        this.fxml = fxml;
        this.sendTransactionDialog = sendTransactionDialog;
        this.context = context;
        this.mainController = mainController;
        this.sendTransactionService = sendTransactionService;
        this.taskConfigurer = taskConfigurer;
        this.reentrantLock = reentrantLock;
        setText("Send");
        construct(this.fxml, this.context);
    }

    public void send() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        mainController.initializeFxml(dialog, fxmlLoader, sendTransactionDialog.getURL());
        dialog.setTitle("Send transaction");
        dialog.getDialogPane().getButtonTypes().add(OK);
        dialog.getDialogPane().getButtonTypes().add(CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == OK) {
            new Thread(buildTask(addressToSend, amountToSend)).start();
            return;
        }
        addressToSend.setText("");
        amountToSend.setText("");
    }

    private Task<Void> buildTask(TextField addressToSend, TextField amountToSend) {
        Task<Void> task = taskConfigurer.configure(
            new SendTransactionTask(
                reentrantLock,
                sendTransactionService,
                amountToSend,
                addressToSend
            ),
            "Sending transaction..."
        );
        task.addEventFilter(WORKER_STATE_SUCCEEDED, event -> {
            amountToSend.setText("");
            addressToSend.setText("");
        });
        return task;
    }
}
