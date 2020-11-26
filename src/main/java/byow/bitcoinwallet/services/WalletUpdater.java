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

    private CurrentAddressesManager currentAddressesManager;

    private String seed;

    private Date walletCreationDate;

    @Autowired
    public WalletUpdater(CurrentAddressesManager currentAddressesManager) {
        this.currentAddressesManager = currentAddressesManager;
    }

    public WalletUpdater updateReceivingAddresses() {
        List<String> addressList = currentAddressesManager.initializeReceivingAddresses(
            initialAddressToMonitor,
            seed,
            walletCreationDate
        );
        int updatedAddressesCount = currentAddressesManager.updateReceivingAddresses(addressList);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            currentAddressesManager.setNextCurrentDerivationPath(initialAddressToMonitor);
            updateReceivingAddresses();
            return this;
        }
        currentAddressesManager.updateNextReceivingAddress(addressList.get(0), updatedAddressesCount, seed, walletCreationDate);
        return this;
    }

    public void updateChangeAddresses() {
        List<String> addressList = currentAddressesManager.initializeChangeAddresses(
            initialAddressToMonitor,
            seed,
            walletCreationDate
        );
        int updatedAddressesCount = currentAddressesManager.updateReceivingAddresses(addressList);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            currentAddressesManager.setNextCurrentChangeDerivationPath(initialAddressToMonitor);
            updateChangeAddresses();
            return;
        }
        currentAddressesManager.updateNextChangeAddress(addressList.get(0), updatedAddressesCount, seed, walletCreationDate);
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
