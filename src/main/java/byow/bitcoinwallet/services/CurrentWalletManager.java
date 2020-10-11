package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Wallet;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.TxOutput;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Component
public class CurrentWalletManager {

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    private final SimpleStringProperty currentReceivingAddress = new SimpleStringProperty();

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>());

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        String receivingAddress = addressGenerator.generate(currentWallet, FIRST_BIP84_ADDRESS_PATH);
        currentReceivingAddress.setValue(receivingAddress);
        Platform.runLater(() -> {
            bitcoindRpcClient.importAddress(receivingAddress, currentWallet.getName(), true);
            List<Unspent> utxos = bitcoindRpcClient.listUnspent(0, 100, receivingAddress);
            Optional<BigDecimal> totalAmmount = utxos.stream().map(TxOutput::amount).reduce(BigDecimal::add);
            OptionalInt confirmations = utxos.stream().mapToInt(Unspent::confirmations).min();
            ReceivingAddress address = new ReceivingAddress(totalAmmount.orElse(BigDecimal.ZERO), confirmations.orElse(0), receivingAddress);
            receivingAddresses.setAll(address);
        });
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

    public ObservableList<ReceivingAddress> getReceivingAddresses() {
        return receivingAddresses;
    }

}
