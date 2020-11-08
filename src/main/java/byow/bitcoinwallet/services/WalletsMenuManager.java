package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.LoadWalletMenuItem;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import com.sun.javafx.collections.ObservableSetWrapper;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.scene.control.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReentrantLock;

@Lazy
@Component
public class WalletsMenuManager {
    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Autowired
    private ReentrantLock reentrantLock;

    @Autowired
    private TaskConfigurer taskConfigurer;

    private final ObservableSet<MenuItem> menuItems = new ObservableSetWrapper<>(new LinkedHashSet<>());

    public void load() {
        walletRepository.findAll().forEach(this::addWallet);
    }

    public void addWallet(Wallet wallet) {
        LoadWalletMenuItem menuItem = new LoadWalletMenuItem(wallet.getName());
        menuItem.setOnAction(click -> new Thread(buildTask(wallet)).start());
        menuItems.add(menuItem);
    }

    private Task<Void> buildTask(Wallet wallet) {
        return taskConfigurer.configure(
            new UpdateCurrentWalletTask(currentWalletManager, reentrantLock, wallet),
            "Loading wallet..."
        );
    }

    public ObservableSet<MenuItem> getMenuItems() {
        return menuItems;
    }
}
