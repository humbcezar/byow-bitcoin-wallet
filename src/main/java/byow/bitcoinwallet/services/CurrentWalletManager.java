package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import javafx.beans.property.SimpleStringProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Component
public class CurrentWalletManager {

    @Autowired
    private AddressGenerator addressGenerator;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    private final SimpleStringProperty currentReceivingAddress = new SimpleStringProperty();

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        currentReceivingAddress.setValue(addressGenerator.generate(currentWallet, FIRST_BIP84_ADDRESS_PATH));
    }

    public String getWalletName() {
        return walletName.get();
    }

    public SimpleStringProperty walletNameProperty() {
        return walletName;
    }

    public String getCurrentReceivingAddress() {
        return currentReceivingAddress.get();
    }

    public SimpleStringProperty currentReceivingAddressProperty() {
        return currentReceivingAddress;
    }
}
