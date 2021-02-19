package byow.bitcoinwallet.services.gui;

import byow.bitcoinwallet.controllers.LoadWalletDialogController;
import byow.bitcoinwallet.entities.LoadWalletMenuItem;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.wallet.CurrentWalletManager;
import byow.bitcoinwallet.tasks.TaskConfigurer;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

@Lazy
@Component
public class WalletsMenuManager {
    private final WalletRepository walletRepository;

    private final CurrentWalletManager currentWalletManager;

    private final ReentrantLock reentrantLock;

    private final TaskConfigurer taskConfigurer;

    private final CurrentTransactions currentTransactions;

    private final ObservableSet<MenuItem> menuItems = new ObservableSetWrapper<>(new LinkedHashSet<>());

    private final DialogService dialogService;

    private final Resource loadWalletDialog;

    @Autowired
    public WalletsMenuManager(
        WalletRepository walletRepository,
        CurrentWalletManager currentWalletManager,
        ReentrantLock reentrantLock,
        TaskConfigurer taskConfigurer,
        CurrentTransactions currentTransactions,
        DialogService dialogService,
        @Value("fxml/load_wallet_dialog.fxml") Resource loadWalletDialog
    ) {
        this.walletRepository = walletRepository;
        this.currentWalletManager = currentWalletManager;
        this.reentrantLock = reentrantLock;
        this.taskConfigurer = taskConfigurer;
        this.currentTransactions = currentTransactions;
        this.dialogService = dialogService;
        this.loadWalletDialog = loadWalletDialog;
    }

    public void load() {
        walletRepository.findAll().forEach(this::addWallet);
    }

    public void addWallet(Wallet wallet) {
        LoadWalletMenuItem menuItem = new LoadWalletMenuItem(wallet.getName());
        menuItem.setOnAction(click -> showDialog(wallet));
        menuItems.add(menuItem);
    }

    private void showDialog(Wallet wallet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        try {
            dialogService.initialize(dialog, fxmlLoader, loadWalletDialog.getURL(), "Load Wallet");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Optional<ButtonType> result = dialog.showAndWait();
        LoadWalletDialogController controller = fxmlLoader.getController();
        if (dialogIsValid(wallet, result, controller)) {
            new Thread(buildTask(wallet)).start();
            return;
        }
        if (result.isPresent() && result.get() != CANCEL) {
            showAlert();
        }
    }

    private void showAlert() {
        Alert alert = new Alert(ERROR);
        alert.setTitle("Error");
        alert.setContentText("Wrong password.");
        alert.show();
    }

    private boolean dialogIsValid(Wallet wallet, Optional<ButtonType> result, LoadWalletDialogController controller) {
        return result.isPresent() && result.get() == OK && controller.passwordIsValid(controller.loadWalletPassword.getText(), wallet.getPassword());
    }

    private Task<Void> buildTask(Wallet wallet) {
        return taskConfigurer.configure(
            new UpdateCurrentWalletTask(currentWalletManager, reentrantLock, wallet, currentTransactions),
            "Loading wallet..."
        );
    }

    public ObservableSet<MenuItem> getMenuItems() {
        return menuItems;
    }
}
