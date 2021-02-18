package byow.bitcoinwallet.services.gui;

import byow.bitcoinwallet.controllers.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
@Lazy
public class DialogService {
    private final ApplicationContext context;

    @Autowired
    public DialogService(ApplicationContext context) {
        this.context = context;
    }

    public void initialize(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader, URL resourceUrl, String dialogTitle) {
        dialog.initOwner(context.getBean(MainController.class).borderPane.getScene().getWindow());
        fxmlLoader.setControllerFactory(context::getBean);
        try {
            fxmlLoader.setLocation(resourceUrl);
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        dialog.setTitle(dialogTitle);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }
}
