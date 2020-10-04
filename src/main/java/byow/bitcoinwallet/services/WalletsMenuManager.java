package byow.bitcoinwallet.services;

import byow.bitcoinwallet.controllers.MainController;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletsMenuManager {
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MainController mainController;

    public void load() {
        walletRepository.findAll().forEach(this::addWallet);
    }

    public void addWallet(Wallet wallet) {
        MenuItem menuItem = new MenuItem(wallet.getName());
        menuItem.setOnAction(click -> setCurrentWallet(wallet));
        mainController.getLoad().getItems().add(menuItem);
    }

    public void setCurrentWallet(Wallet wallet) {
        Stage stage = (Stage) mainController.getBorderPane().getScene().getWindow();
        stage.setTitle("BYOW Wallet - ".concat(wallet.getName()));
        mainController.setCurrentWallet(wallet);
    }
}
