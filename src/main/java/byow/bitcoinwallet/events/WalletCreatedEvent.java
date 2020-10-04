package byow.bitcoinwallet.events;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.context.ApplicationEvent;

public class WalletCreatedEvent extends ApplicationEvent {
    private final Wallet wallet;

    public WalletCreatedEvent(Object source, Wallet wallet) {
        super(source);
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }
}
