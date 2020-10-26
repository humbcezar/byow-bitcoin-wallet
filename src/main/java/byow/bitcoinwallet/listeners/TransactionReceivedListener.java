package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import byow.bitcoinwallet.services.CurrentWalletManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Lazy
public class TransactionReceivedListener implements ApplicationListener<TransactionReceivedEvent> {
    @Autowired
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        event.getRawTransaction()
                .vOut()
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
