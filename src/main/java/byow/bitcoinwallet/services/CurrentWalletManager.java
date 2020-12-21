package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class CurrentWalletManager {

    private List<CurrentAddressesManager> currentAddressesManagers;

    private MultiAddressUpdater multiAddressUpdater;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    @Autowired
    public CurrentWalletManager(List<CurrentAddressesManager> currentAddressesManagers, MultiAddressUpdater multiAddressUpdater) {
        this.currentAddressesManagers = currentAddressesManagers;
        this.multiAddressUpdater = multiAddressUpdater;
    }

    public void updateCurrentWallet(Wallet currentWallet) {
        currentAddressesManagers.forEach(CurrentAddressesManager::clear);
        this.currentWallet = currentWallet;

        multiAddressUpdater.update(currentWallet);
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

    public void setWalletName(String walletName) {
        this.walletName.set(walletName);
    }
}
