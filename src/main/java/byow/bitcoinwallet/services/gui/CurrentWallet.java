package byow.bitcoinwallet.services.gui;

import byow.bitcoinwallet.entities.Wallet;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CurrentWallet {
    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    public String getWalletName() {
        return walletName.get();
    }

    public SimpleStringProperty walletNameProperty() {
        return walletName;
    }

    public Wallet getCurrentWallet() {
        return currentWallet;
    }

    public void setWalletName(String walletName) {
        this.walletName.set(walletName);
    }

    public void setCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
    }
}
