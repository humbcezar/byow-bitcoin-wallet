package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NestedSegwitAddressUpdater implements AddressUpdater {
    private int initialAddressToMonitor;

    private CurrentNestedSegwitAddressesManager currentAddressesManager;

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }

    @Autowired
    public NestedSegwitAddressUpdater(CurrentNestedSegwitAddressesManager currentAddressesManager) {
        this.currentAddressesManager = currentAddressesManager;
    }

    @Override
    public void update(Wallet wallet) {
        currentAddressesManager.update(wallet, initialAddressToMonitor);
    }
}
