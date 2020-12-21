package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultiAddressUpdater {

    private List<AddressUpdater> addressUpdaters;

    private CurrentReceivingAddresses currentReceivingAddresses;

    @Autowired
    public MultiAddressUpdater(List<AddressUpdater> addressUpdaters, CurrentReceivingAddresses currentReceivingAddresses) {
        this.addressUpdaters = addressUpdaters;
        this.currentReceivingAddresses = currentReceivingAddresses;
    }

    public void update(Wallet currentWallet) {
        currentReceivingAddresses.clear();
        addressUpdaters.forEach(addressUpdater ->
            addressUpdater.update(currentWallet.getSeed(), currentWallet.getCreatedAt())
        );
    }
}
