package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class CurrentWalletManager {

    @Autowired
    private WalletUpdater walletUpdater;

    @Autowired
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    public void updateCurrentWallet(Wallet currentWallet) {
        currentReceivingAddressesManager.clear();
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());

        runUpdateTask(currentWallet);
    }

    public void updateCurrentWallet() {
        if (currentWallet != null) {
            runUpdateTask(currentWallet);
        }
    }

    private void runUpdateTask(Wallet currentWallet) {
        walletUpdater.setSeed(currentWallet.getSeed())
            .setDate(currentWallet.getCreatedAt())
            .update();
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
