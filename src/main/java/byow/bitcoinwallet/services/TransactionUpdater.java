package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.NextReceivingAddress;
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
    private CurrentAddressesManager currentAddressesManager;

    private CurrentWalletManager currentWalletManager;

    private NextReceivingAddress nextReceivingAddress;

    private NextChangeAddress nextChangeAddress;

    private String addressPrefix;

    @Autowired
    public TransactionUpdater(
        CurrentAddressesManager currentAddressesManager,
        CurrentWalletManager currentWalletManager,
        NextReceivingAddress nextReceivingAddress,
        NextChangeAddress nextChangeAddress,
        @Qualifier("addressPrefix") String addressPrefix
    ) {
        this.currentAddressesManager = currentAddressesManager;
        this.currentWalletManager = currentWalletManager;
        this.nextReceivingAddress = nextReceivingAddress;
        this.nextChangeAddress = nextChangeAddress;
        this.addressPrefix = addressPrefix;
    }

    public void update(Object transaction) {
        concat(parseOutputAddresses(transaction), parseInputAddresses(transaction))
            .distinct()
            .peek(address ->
                currentAddressesManager.initializeReceivingAddresses(
                    1,
                    currentWalletManager.getCurrentWallet().getSeed(),
                    currentWalletManager.getCurrentWallet().getCreatedAt()
                )
            )
            .peek(address ->
                currentAddressesManager.initializeChangeAddresses(
                    1,
                    currentWalletManager.getCurrentWallet().getSeed(),
                    currentWalletManager.getCurrentWallet().getCreatedAt()
                )
            )
            .filter(address -> currentAddressesManager.contains(address))
            .peek(address -> currentAddressesManager.updateReceivingAddresses(List.of(address)))
            .forEach(address -> {
                if(nextReceivingAddress.equalAddress(address)) {
                    currentAddressesManager.updateNextReceivingAddress(
                        "",
                        1,
                        currentWalletManager.getCurrentWallet().getSeed(),
                        currentWalletManager.getCurrentWallet().getCreatedAt()
                    );
                    return;
                }
                if (nextChangeAddress.equalAddress(address)) {
                    currentAddressesManager.updateNextChangeAddress(
                        "",
                        1,
                        currentWalletManager.getCurrentWallet().getSeed(),
                        currentWalletManager.getCurrentWallet().getCreatedAt()
                    );
                }
            });
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
