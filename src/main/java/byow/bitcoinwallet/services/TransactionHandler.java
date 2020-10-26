package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

import java.util.List;

@Component
@Lazy
public class TransactionHandler {
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private CurrentWalletManager currentWalletManager;

    @Autowired
    public TransactionHandler(
            CurrentReceivingAddressesManager currentReceivingAddressesManager,
            CurrentWalletManager currentWalletManager
    ) {
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        this.currentWalletManager = currentWalletManager;
    }

    public void handle(RawTransaction transaction) {
        transaction.vOut()
            .stream()
            .filter(vout -> vout.scriptPubKey().addresses() != null)
            .forEach(vout ->
                vout.scriptPubKey()
                    .addresses()
                    .stream()
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
