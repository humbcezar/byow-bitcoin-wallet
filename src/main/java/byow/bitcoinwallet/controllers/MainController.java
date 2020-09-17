package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.controllers.CreateWalletDialogController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class MainController {
    @FXML
    private BorderPane borderPane;
    @Value("fxml/create_wallet_dialog.fxml")
    private Resource createWalletDialog;
    @Autowired
    ApplicationContext context;

    public void exit() {
        Platform.exit();
    }

    public void openCreateWalletDialog(ActionEvent actionEvent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(borderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(context::getBean);
        try {
            fxmlLoader.setLocation(createWalletDialog.getURL());
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dialog.setTitle("Create New Wallet");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            CreateWalletDialogController controller = fxmlLoader.getController();
            controller.createWallet();
        }
    }
}
