package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultAddressUpdater implements AddressUpdater {
    private int initialAddressToMonitor;

    private CurrentDefaultAddressesManager currentAddressesManager;

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }

    @Autowired
    public DefaultAddressUpdater(CurrentDefaultAddressesManager currentAddressesManager) {
        this.currentAddressesManager = currentAddressesManager;
    }

    @Override
    public void update(Wallet wallet) {
        currentAddressesManager.update(wallet, initialAddressToMonitor);
    }
}
