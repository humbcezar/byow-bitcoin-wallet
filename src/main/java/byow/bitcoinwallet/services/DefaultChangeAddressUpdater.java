package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DefaultChangeAddressUpdater implements AddressUpdater {
    private int initialAddressToMonitor;

    private CurrentDefaultChangeAddressesManager currentAddressesManager;

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }

    @Autowired
    public DefaultChangeAddressUpdater(CurrentDefaultChangeAddressesManager currentAddressesManager) {
        this.currentAddressesManager = currentAddressesManager;
    }

    @Override
    public void update(String seed, Date walletCreationDate) {
        currentAddressesManager.update(seed, walletCreationDate, initialAddressToMonitor);
    }
}