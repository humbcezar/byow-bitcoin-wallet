package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.UtxosGetter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

import static java.util.List.of;

@Component
@Lazy
public class AddressUpdater {
    private final List<CurrentAddressesManager> currentAddressesManagers;

    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final UtxosGetter utxosGetter;

    public AddressUpdater(
        List<CurrentAddressesManager> currentAddressesManagers,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter
    ) {
        this.currentAddressesManagers = currentAddressesManagers;
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.utxosGetter = utxosGetter;
    }

    public void update(Stream<String> addresses, Wallet wallet) {
        addresses.peek(address -> currentReceivingAddressesUpdater.updateReceivingAddresses(
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
