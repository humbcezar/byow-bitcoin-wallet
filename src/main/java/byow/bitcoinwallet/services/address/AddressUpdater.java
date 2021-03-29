package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.UtxosGetter;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.util.List.of;

@Component
@Lazy
public class AddressUpdater {
    private final List<CurrentAddressesManager> currentAddressesManagers;

    private final CurrentReceivingAddresses currentReceivingAddresses;

    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final UtxosGetter utxosGetter;

    public AddressUpdater(
        List<CurrentAddressesManager> currentAddressesManagers,
        CurrentReceivingAddresses currentReceivingAddresses,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter
    ) {
        this.currentAddressesManagers = currentAddressesManagers;
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.utxosGetter = utxosGetter;
    }

    public void update(Stream<String> addresses, Wallet wallet) {
        addresses.distinct()
            .peek(address -> currentAddressesManagers.forEach(
                currentAddressManager -> currentAddressManager.initializeAddresses(
                    1,
                    wallet.getXPub(currentAddressManager.getXPubType()).getKey(),
                    wallet.getCreatedAt(),
                    wallet.getName()
                ))
            )
            .filter(currentReceivingAddresses::contains)
            .peek(address -> currentReceivingAddressesUpdater.updateReceivingAddresses(
                of(address),
                utxosGetter.getUtxos(of(address)))
            )
            .forEach(address -> currentAddressesManagers.stream()
                .filter(currentAddressManager -> currentAddressManager.getNextAddress().equalAddress(address))
                .forEach(currentAddressManager -> currentAddressManager.updateNextAddress(
                        "",
                        1,
                        wallet.getXPub(currentAddressManager.getXPubType()).getKey(),
                        wallet.getCreatedAt(),
                        wallet.getName()
                    )
                )
            );
    }
}
