package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.sun.javafx.collections.ImmutableObservableList;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;


@Component
public class WalletsMenuManager {
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    private final ObservableList<MenuItem> menuItems = new ObservableListWrapper<>(new LinkedList<>());

    public void load() {
        walletRepository.findAll().forEach(this::addWallet);
    }

    public void addWallet(Wallet wallet) {
        MenuItem menuItem = new MenuItem(wallet.getName());
        menuItem.setOnAction(click -> currentWalletManager.updateCurrentWallet(wallet));
        menuItems.add(menuItem);
    }

    public ObservableList<MenuItem> getMenuItems() {
        return menuItems;
    }
}
