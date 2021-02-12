package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class MultiAddressUpdater {

    private List<AddressUpdater> addressUpdaters;

    private CurrentReceivingAddresses currentReceivingAddresses;

    private CurrentTransactions currentTransactions;

    @Autowired
    public MultiAddressUpdater(
        List<AddressUpdater> addressUpdaters,
        CurrentReceivingAddresses currentReceivingAddresses,
        CurrentTransactions currentTransactions
    ) {
        this.addressUpdaters = addressUpdaters;
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.currentTransactions = currentTransactions;
    }

    public void update(Wallet currentWallet) {
        currentReceivingAddresses.clear();
        currentTransactions.clear();
        addressUpdaters.forEach(addressUpdater ->
            addressUpdater.update(currentWallet)
        );
    }
}
