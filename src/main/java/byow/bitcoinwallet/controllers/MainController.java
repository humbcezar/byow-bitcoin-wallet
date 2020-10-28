package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.CurrentWalletManager;
import byow.bitcoinwallet.services.WalletsMenuManager;
import byow.bitcoinwallet.tasks.TransactionTask;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
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
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Lazy
@Component
public class MainController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Menu load;

    @Value("fxml/create_wallet_dialog.fxml")
    private Resource createWalletDialog;

    @Value("fxml/import_wallet_dialog.fxml")
    private Resource importWalletDialog;

    @Autowired
    private UpdateCurrentWalletTask updateCurrentWalletTask;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private WalletsMenuManager walletsMenuManager;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Autowired
    private TransactionTask transactionTask;

    @FXML
    public void initialize() {
        walletsMenuManager.getMenuItems().addListener(
            (SetChangeListener<MenuItem>) change -> Platform.runLater(
                () -> {
                    Set<String> menuTexts = load.getItems().stream().map(MenuItem::getText).collect(Collectors.toSet());
                    if (!menuTexts.contains(change.getElementAdded().getText())) {
                        load.getItems().addAll(change.getElementAdded());
                    }
                }
            )
        );
        currentWalletManager.walletNameProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                Stage stage = (Stage) borderPane.getScene().getWindow();
                stage.setTitle("BYOW Wallet - ".concat(newValue));
            })
        );
        walletsMenuManager.load();
        Thread thread = new Thread(transactionTask.getTask());
        thread.setDaemon(true);
        thread.start();
    }

    public void exit() {
        transactionTask.close();
        updateCurrentWalletTask.cancel();
        Platform.exit();
    }

    public void openCreateWalletDialog() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        initializeFxml(dialog, fxmlLoader, createWalletDialog.getURL());
        configureDialog(dialog, fxmlLoader, "Create New Wallet");
    }

    public void openImportWalletDialog() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        initializeFxml(dialog, fxmlLoader, importWalletDialog.getURL());
        configureDialog(dialog, fxmlLoader, "Import Wallet");
    }

    private void configureDialog(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader, String dialogTitle) {
        dialog.setTitle(dialogTitle);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        GenerateWalletDialogController controller = fxmlLoader.getController();
        dialog.getDialogPane()
            .lookupButton(ButtonType.OK)
            .disableProperty()
            .bind(controller.getAllInputsAreFull().not());

        showDialog(dialog, controller);
    }

    private void showDialog(Dialog<ButtonType> dialog, GenerateWalletDialogController controller) {
        Optional<ButtonType> result = dialog.showAndWait();
        createWallet(controller, result);
    }

    private void createWallet(GenerateWalletDialogController controller, Optional<ButtonType> result) {
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

    private void initializeFxml(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader, URL resourceUrl) {
        dialog.initOwner(borderPane.getScene().getWindow());
        fxmlLoader.setControllerFactory(context::getBean);
        try {
            fxmlLoader.setLocation(resourceUrl);
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Menu getLoad() {
        return load;
    }
}
