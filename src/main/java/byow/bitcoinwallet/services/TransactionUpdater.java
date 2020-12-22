package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.blockstream.libwally.Wally.*;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;

@Component
@Lazy
public class TransactionUpdater {
    private List<CurrentAddressesManager> currentAddressesManagers;

    private CurrentReceivingAddresses currentReceivingAddresses;

    private CurrentWalletManager currentWalletManager;

    private String addressPrefix;

    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    @Autowired
    public TransactionUpdater(
        List<CurrentAddressesManager> currentAddressesManagers,
        CurrentWalletManager currentWalletManager,
        @Qualifier("addressPrefix") String addressPrefix,
        CurrentReceivingAddresses currentReceivingAddresses,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater
    ) {
        this.currentAddressesManagers = currentAddressesManagers;
        this.currentWalletManager = currentWalletManager;
        this.addressPrefix = addressPrefix;
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
    }

    public void update(Object transaction) {
        concat(parseOutputAddresses(transaction), parseInputAddresses(transaction))
            .distinct()
            .peek(address -> currentAddressesManagers.forEach(
                currentAddressManager -> currentAddressManager.initializeAddresses(
                    1,
                    currentWalletManager.getCurrentWallet().getSeed(),
                    currentWalletManager.getCurrentWallet().getCreatedAt()
                ))
            )
            .filter(address -> currentReceivingAddresses.contains(address))
            .peek(address -> currentReceivingAddressesUpdater.updateReceivingAddresses(List.of(address)))
            .forEach(address -> currentAddressesManagers.stream()
                .filter(currentAddressManager -> currentAddressManager.getNextAddress().equalAddress(address))
                .forEach(currentAddressManager -> currentAddressManager.updateNextAddress(
                    "",
                    1,
                    currentWalletManager.getCurrentWallet().getSeed(),
                    currentWalletManager.getCurrentWallet().getCreatedAt()
                ))
            );
    }

    private Stream<String> parseInputAddresses(Object transaction) {
        List<String> inputAddresses = new ArrayList<>();
        if(!tx_is_coinbase(transaction)) {
            int numInputs = tx_get_num_inputs(transaction);
            inputAddresses = range(0, numInputs).mapToObj(i -> {
                byte[] publicKey = tx_get_input_witness(transaction, i, 1);
                byte[] witness = witness_program_from_bytes(publicKey, WALLY_SCRIPT_HASH160);
                return addr_segwit_from_bytes(witness, addressPrefix, 0);
            }).collect(Collectors.toList());
        }
        return inputAddresses.stream();
    }

    private Stream<String> parseOutputAddresses(Object transaction) {
        return range(0, tx_get_num_outputs(transaction)).mapToObj(i -> {
            try {
                return addr_segwit_from_bytes(tx_get_output_script(transaction, i), addressPrefix, 0);
            } catch (IllegalArgumentException ignored) {
                return "";
            }
        });
    }
}
