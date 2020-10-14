package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.CurrentWalletManager;
import byow.bitcoinwallet.services.WalletsMenuManager;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Lazy
@Component
public class MainController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Menu load;

    @Value("fxml/create_wallet_dialog.fxml")
    private Resource createWalletDialog;

    @Autowired
    private UpdateCurrentWalletTask updateCurrentWalletTask;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private WalletsMenuManager walletsMenuManager;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @FXML
    public void initialize() {
        walletsMenuManager.getMenuItems().addListener(
                (ListChangeListener<MenuItem>) change -> Platform.runLater(
                        () -> load.getItems().setAll(change.getList())
                )
        );
        currentWalletManager.walletNameProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                Stage stage = (Stage) borderPane.getScene().getWindow();
                stage.setTitle("BYOW Wallet - ".concat(newValue));
            })
        );
        walletsMenuManager.load();
    }

    public void exit() {
        Platform.exit();
    }

    public void openCreateWalletDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        initializeFxml(dialog, fxmlLoader);
        configureDialog(dialog, fxmlLoader);
    }

    private void configureDialog(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader) {
        dialog.setTitle("Create New Wallet");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        CreateWalletDialogController controller = fxmlLoader.getController();
        dialog.getDialogPane()
            .lookupButton(ButtonType.OK)
            .disableProperty()
            .bind(controller.getAllInputsAreFull().not());

        showDialog(dialog, controller);
    }

    private void showDialog(Dialog<ButtonType> dialog, CreateWalletDialogController controller) {
        Optional<ButtonType> result = dialog.showAndWait();
        createWallet(controller, result);
    }

    private void createWallet(CreateWalletDialogController controller, Optional<ButtonType> result) {
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                controller.createWallet();
            } catch (DataIntegrityViolationException exception) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Could not create wallet: A wallet with the same name already exists.");
                alert.show();
            }
        }
    }

    private void initializeFxml(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader) {
        dialog.initOwner(borderPane.getScene().getWindow());
        fxmlLoader.setControllerFactory(context::getBean);
        try {
            fxmlLoader.setLocation(createWalletDialog.getURL());
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Menu getLoad() {
        return load;
    }

}
