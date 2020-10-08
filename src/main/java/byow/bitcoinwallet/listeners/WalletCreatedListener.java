package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.services.CurrentWalletManager;
import byow.bitcoinwallet.services.WalletsMenuManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class WalletCreatedListener implements ApplicationListener<WalletCreatedEvent> {

    @Autowired
    WalletsMenuManager walletsMenuManager;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Override
    public void onApplicationEvent(WalletCreatedEvent event) {
        walletsMenuManager.addWallet(event.getWallet());
        currentWalletManager.updateCurrentWallet(event.getWallet());
    }
}
