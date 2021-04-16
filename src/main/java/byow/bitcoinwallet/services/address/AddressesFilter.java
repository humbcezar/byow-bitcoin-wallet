package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@Lazy
public class AddressesFilter {
    private final List<CurrentAddressesManager> currentAddressesManagers;

    private final CurrentReceivingAddresses currentReceivingAddresses;

    private final CurrentWallet currentWallet;

    public AddressesFilter(
        List<CurrentAddressesManager> currentAddressesManagers,
        CurrentReceivingAddresses currentReceivingAddresses,
        CurrentWallet currentWallet
    ) {
        this.currentAddressesManagers = currentAddressesManagers;
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.currentWallet = currentWallet;
    }

    public Stream<String> filterAddresses(Stream<String> addresses) {
        return filter(addresses, currentWallet.getCurrentWallet());
    }

    private Stream<String> filter(Stream<String> addresses, Wallet wallet) {
        return addresses.distinct()
            .peek(address -> currentAddressesManagers.forEach(
                currentAddressManager -> currentAddressManager.initializeAddresses(
                    1,
                    wallet.getXPub(currentAddressManager.getXPubType()).getKey(),
                    wallet.getCreatedAt(),
                    wallet.getName()
                ))
            )
            .filter(currentReceivingAddresses::contains);
    }
}
