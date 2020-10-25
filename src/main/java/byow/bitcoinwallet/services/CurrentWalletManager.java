package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class CurrentWalletManager {

    @Autowired
    private UpdateCurrentWalletTask updateCurrentWalletTask;

    @Autowired
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    private Task<Void> task;

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        currentReceivingAddressesManager.clear();

        if (task != null) {
            task.cancel();
        }
        task = updateCurrentWalletTask.setSeed(currentWallet.getSeed()).getTask();
        new Thread(task).start();
    }

    public String getWalletName() {
        return walletName.get();
    }

    public SimpleStringProperty walletNameProperty() {
        return walletName;
    }

}
