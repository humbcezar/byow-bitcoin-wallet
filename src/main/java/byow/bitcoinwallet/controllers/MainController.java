package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.gui.DialogService;
import byow.bitcoinwallet.services.gui.WalletsMenuManager;
import byow.bitcoinwallet.tasks.NodeMonitorTask;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Lazy
@Component
public class MainController {

    @FXML
    public BorderPane borderPane;

    @FXML
    private Menu load;

    private final Resource createWalletDialog;

    private final Resource importWalletDialog;

    private final WalletsMenuManager walletsMenuManager;

    private final CurrentWallet currentWallet;

    private final NodeMonitorTask nodeMonitorTask;

    private final DialogService dialogService;

    @Autowired
    public MainController(
        @Value("fxml/create_wallet_dialog.fxml") Resource createWalletDialog,
        @Value("fxml/import_wallet_dialog.fxml") Resource importWalletDialog,
        WalletsMenuManager walletsMenuManager,
        CurrentWallet currentWallet,
        NodeMonitorTask nodeMonitorTask,
        DialogService dialogService
    ) {
        this.createWalletDialog = createWalletDialog;
        this.importWalletDialog = importWalletDialog;
        this.walletsMenuManager = walletsMenuManager;
        this.currentWallet = currentWallet;
        this.nodeMonitorTask = nodeMonitorTask;
        this.dialogService = dialogService;
    }

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
        currentWallet.walletNameProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                Stage stage = (Stage) borderPane.getScene().getWindow();
                stage.setTitle("BYOW Wallet - ".concat(newValue));
            })
        );
        walletsMenuManager.load();
        Thread thread = new Thread(nodeMonitorTask.buildTask());
        thread.setDaemon(true);
        thread.start();
        nodeMonitorTask.subscribe();
    }

    public void exit() {
        nodeMonitorTask.close();
        Platform.exit();
    }

    public void openCreateWalletDialog() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        dialogService.initialize(dialog, fxmlLoader, createWalletDialog.getURL(), "Create New Wallet");
        configureDialog(dialog, fxmlLoader);
    }

    public void openImportWalletDialog() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        dialogService.initialize(dialog, fxmlLoader, importWalletDialog.getURL(), "Import Wallet");
        configureDialog(dialog, fxmlLoader);
    }

    private void configureDialog(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader) {
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

    public Menu getLoad() {
        return load;
    }
}
