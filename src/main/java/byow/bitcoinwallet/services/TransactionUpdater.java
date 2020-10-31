package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

import java.util.List;

@Component
@Lazy
public class TransactionUpdater {
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private CurrentWalletManager currentWalletManager;

    private NextReceivingAddress nextReceivingAddress;

    @Autowired
    public TransactionUpdater(
            CurrentReceivingAddressesManager currentReceivingAddressesManager,
            CurrentWalletManager currentWalletManager,
            NextReceivingAddress nextReceivingAddress
    ) {
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        this.currentWalletManager = currentWalletManager;
        this.nextReceivingAddress = nextReceivingAddress;
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
                    .peek(address -> currentReceivingAddressesManager.updateReceivingAddresses(
                            List.of(address),
                            currentWalletManager.getCurrentWallet().getCreatedAt()
                        )
                    )
                    .filter(address -> nextReceivingAddress.equalAddress(address))
                    .forEach(address ->
                        currentReceivingAddressesManager.updateNextAddress(
                            "",
                            1,
                            currentWalletManager.getCurrentWallet().getSeed()
                        )
                    )
            );
    }
}
