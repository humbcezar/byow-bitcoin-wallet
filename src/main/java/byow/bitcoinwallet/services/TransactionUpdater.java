package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Lazy
public class TransactionUpdater {
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private CurrentWalletManager currentWalletManager;

    @Autowired
    public TransactionUpdater(
            CurrentReceivingAddressesManager currentReceivingAddressesManager,
            CurrentWalletManager currentWalletManager
    ) {
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        this.currentWalletManager = currentWalletManager;
    }

    public void update(RawTransaction transaction) {
        transaction.vOut()
            .stream()
            .filter(vout -> vout.scriptPubKey().addresses() != null)
            .forEach(vout ->
                vout.scriptPubKey()
                    .addresses()
                    .stream()
                    .peek(address ->
                        currentReceivingAddressesManager.initializeReceivingAddresses(
                            1,
                            currentWalletManager.getCurrentWallet().getSeed()
                        )
                    )
                    .filter(address -> currentReceivingAddressesManager.contains(address))
                    .map(address -> currentReceivingAddressesManager.updateReceivingAddresses(
                            List.of(address),
                            currentWalletManager.getCurrentWallet().getCreatedAt()
                        )
                    )
                    .reduce(Integer::sum)
                    .ifPresent(sum ->
                        currentReceivingAddressesManager.updateNextAddress(
                            "",
                            sum,
                            currentWalletManager.getCurrentWallet().getSeed()
                        )
                    )
            );
    }
}
