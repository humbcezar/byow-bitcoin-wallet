package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Lazy
@Component
public class WalletUpdater {
    private int initialAddressToMonitor;

    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private String seed;

    private Date walletCreationDate;

    @Autowired
    public WalletUpdater(CurrentReceivingAddressesManager currentReceivingAddressesManager) {
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
    }

    public void update() {
        List<String> addressList = currentReceivingAddressesManager.initializeReceivingAddresses(
                initialAddressToMonitor,
                seed,
                walletCreationDate
        );
        int updatedAddressesCount = currentReceivingAddressesManager.updateReceivingAddresses(addressList);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            currentReceivingAddressesManager.setNextCurrentDerivationPath(initialAddressToMonitor);
            update();
            return;
        }
        currentReceivingAddressesManager.updateNextAddress(addressList.get(0), updatedAddressesCount, seed, walletCreationDate);
    }

    public WalletUpdater setSeed(String seed) {
        this.seed = seed;
        return this;
    }

    public WalletUpdater setDate(Date createdAt) {
        walletCreationDate = createdAt;
        return this;
    }

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }
}
