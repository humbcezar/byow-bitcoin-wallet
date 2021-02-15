package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class MultiAddressUpdater {

    private final List<CurrentAddressesManager> addressManagers;

    private final CurrentReceivingAddresses currentReceivingAddresses;

    private final CurrentTransactions currentTransactions;

    private int initialAddressToMonitor;

    @Autowired
    public MultiAddressUpdater(
        List<CurrentAddressesManager> addressManagers,
        CurrentReceivingAddresses currentReceivingAddresses,
        CurrentTransactions currentTransactions
    ) {
        this.addressManagers = addressManagers;
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.currentTransactions = currentTransactions;
    }

    public void update(Wallet currentWallet) {
        currentReceivingAddresses.clear();
        currentTransactions.clear();
        addressManagers.forEach(addressManager ->
            addressManager.update(currentWallet, initialAddressToMonitor)
        );
    }

    @Value("${bitcoin.initial_addresses_to_monitor}")
    public void setInitialAddressToMonitor(int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }
}