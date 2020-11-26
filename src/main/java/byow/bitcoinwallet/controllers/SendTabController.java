package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.SendTransactionService;
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
import java.math.BigDecimal;
import java.util.Optional;

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

    @Autowired
    public SendTabController(
        @Value("classpath:/fxml/send_tab.fxml") Resource fxml,
        @Value("fxml/send_transaction_dialog.fxml") Resource sendTransactionDialog,
        ApplicationContext context,
        MainController mainController,
        SendTransactionService sendTransactionService
    ) throws IOException {
        this.fxml = fxml;
        this.sendTransactionDialog = sendTransactionDialog;
        this.context = context;
        this.mainController = mainController;
        this.sendTransactionService = sendTransactionService;
        setText("Send");
        construct(this.fxml, this.context);
    }

    public void send() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        mainController.initializeFxml(dialog, fxmlLoader, sendTransactionDialog.getURL());
        dialog.setTitle("Send transaction");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            sendTransactionService.send(
                addressToSend.getText(),
                new BigDecimal(amountToSend.getText())
            );
        }
    }
}
