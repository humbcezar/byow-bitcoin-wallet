package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.LoadWalletMenuItem;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.collections.ObservableSet;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

@Lazy
@Component
public class WalletsMenuManager {
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    private final ObservableSet<MenuItem> menuItems = new ObservableSetWrapper<>(new LinkedHashSet<>());

    public void load() {
        walletRepository.findAll().forEach(this::addWallet);
    }

    public void addWallet(Wallet wallet) {
        LoadWalletMenuItem menuItem = new LoadWalletMenuItem(wallet.getName());
        menuItem.setOnAction(click -> currentWalletManager.updateCurrentWallet(wallet));
        menuItems.add(menuItem);
    }

    public ObservableSet<MenuItem> getMenuItems() {
        return menuItems;
    }
}
