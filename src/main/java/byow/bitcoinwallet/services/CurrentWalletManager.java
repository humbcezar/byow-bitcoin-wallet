package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.CurrentReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Wallet;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.TxOutput;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.*;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Component
public class CurrentWalletManager {

    @Value("${bitcoin.initial_addresses_to_monitor}")
    private int initialAddressToMonitor;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private MultiAddressesImporter multiAddressesImporter;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    private final CurrentReceivingAddress currentReceivingAddress = new CurrentReceivingAddress();

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>());

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        receivingAddresses.clear();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<String> addressList = new LinkedList<>();
                DerivationPath addressPath = FIRST_BIP84_ADDRESS_PATH;
                for (int i = 0; i < initialAddressToMonitor; i++) {
                    addressList.add(addressGenerator.generate(currentWallet.getSeed(), addressPath));
                    addressPath = addressPath.next();
                }
                multiAddressesImporter.importMultiAddresses(addressList.toArray(new String[0]));
                List<Unspent> utxos = bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
                LinkedHashMap<String, ReceivingAddress> usedReceivingAddressMap = new LinkedHashMap<>();
                utxos.forEach(
                        utxo -> usedReceivingAddressMap.putIfAbsent(utxo.address(), buildReceivingAddress(usedReceivingAddressMap, utxo))
                );
                receivingAddresses.setAll(usedReceivingAddressMap.values());
                String lastUsedAddress = utxos.get(utxos.size() - 1).address();
                currentReceivingAddress.setReceivingAddress(
                        new ReceivingAddress(BigDecimal.ZERO, 0, addressList.get(addressList.indexOf(lastUsedAddress) + 1))
                );

                return null;
            }
        };
        new Thread(task).start();
    }

    private ReceivingAddress buildReceivingAddress(LinkedHashMap<String, ReceivingAddress> receivingAddresses, Unspent utxo) {
        ReceivingAddress address = receivingAddresses.getOrDefault(utxo.address(), null);
        if (address == null) {
            return new ReceivingAddress(utxo.amount(), utxo.confirmations(), utxo.address());
        }
        if (utxo.confirmations() < address.getConfirmations()) {
            address.setConfirmations(utxo.confirmations());
        }
        address.setBalance(utxo.amount() + address.getBalance());
        return address;
    }

    public String getWalletName() {
        return walletName.get();
    }

    public SimpleStringProperty walletNameProperty() {
        return walletName;
    }

    public ObservableList<ReceivingAddress> getReceivingAddresses() {
        return receivingAddresses;
    }

    public ReceivingAddress getCurrentReceivingAddress() {
        return currentReceivingAddress.getValue();
    }

    public ObservableValue<ReceivingAddress> currentReceivingAddressProperty() {
        return currentReceivingAddress;
    }
}
