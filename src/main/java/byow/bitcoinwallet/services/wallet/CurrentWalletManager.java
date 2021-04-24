package byow.bitcoinwallet.services.wallet;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.address.CurrentAddressesManager;
import byow.bitcoinwallet.services.address.MultiAddressUpdater;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class CurrentWalletManager {

    private final List<CurrentAddressesManager> currentAddressesManagers;

    private final MultiAddressUpdater multiAddressUpdater;

    private final CurrentWallet currentWallet;

    @Autowired
    public CurrentWalletManager(
        List<CurrentAddressesManager> currentAddressesManagers,
        MultiAddressUpdater multiAddressUpdater,
        CurrentWallet currentWallet
    ) {
        this.currentAddressesManagers = currentAddressesManagers;
        this.multiAddressUpdater = multiAddressUpdater;
        this.currentWallet = currentWallet;
    }

    public void updateCurrentWallet(Wallet currentWallet) {
        currentAddressesManagers.forEach(CurrentAddressesManager::clear);
        this.currentWallet.setCurrentWallet(currentWallet);
        multiAddressUpdater.update(currentWallet.isWatchOnly() ? currentWallet.getParent() : currentWallet);
    }
}
