package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import javafx.beans.property.SimpleStringProperty;
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

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        currentReceivingAddressesManager.clear();

        updateCurrentWalletTask.cancel();
        new Thread(
            updateCurrentWalletTask.setSeed(currentWallet.getSeed())
                    .setDate(currentWallet.getCreatedAt())
                    .getTask()
        ).start();
    }

    public String getWalletName() {
        return walletName.get();
    }

    public SimpleStringProperty walletNameProperty() {
        return walletName;
    }

    public Wallet getCurrentWallet() {
        return currentWallet;
    }

}
