package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.*;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Component
public class CurrentReceivingAddressesManager {
    @Autowired
    private AddressSequentialGenerator addressSequentialGenerator;
    @Autowired
    private NextReceivingAddress nextReceivingAddress;

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>(),
        receivingAddress -> new Observable[]{receivingAddress.balanceProperty(), receivingAddress.confirmationsProperty()}
    );

    private final LinkedHashMap<String, ReceivingAddress> receivingAddressesMap = new LinkedHashMap<>();

    private DerivationPath currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;

    public void clear() {
        receivingAddresses.clear();
        receivingAddressesMap.clear();
        currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;
        nextReceivingAddress.setReceivingAddress(
                new ReceivingAddress(BigDecimal.ZERO, 0, "")
        );
    }

    public void initializeReceivingAddresses(List<String> addresses) {
        addresses.forEach(address -> {
            ReceivingAddress receivingAddress = new ReceivingAddress(BigDecimal.ZERO, -1, address);
            receivingAddressesMap.put(address, receivingAddress);
            receivingAddresses.add(receivingAddress);
        });
    }

    public int updateReceivingAddresses(List<Unspent> utxos) {
        Set<ReceivingAddress> changeSet = new HashSet<>();
        utxos.forEach(utxo -> updateReceivingAddress(utxo, changeSet));
        return changeSet.size();
    }

    private void updateReceivingAddress(Unspent utxo, Set<ReceivingAddress> changeSet) {
        ReceivingAddress receivingAddress = receivingAddressesMap.get(utxo.address());
        receivingAddress.setBalance(utxo.amount().add(new BigDecimal(receivingAddress.getBalance())).toString());
        if (receivingAddress.getConfirmations() == -1 || utxo.confirmations() < receivingAddress.getConfirmations()) {
            receivingAddress.setConfirmations(utxo.confirmations());
        }
        changeSet.add(receivingAddress);
    }

    public void updateNextAddress(String address, int updatedAddressesCount, String seed) {
        if (updatedAddressesCount > 0) {
            address = addressSequentialGenerator.deriveAddresses(
                    1,
                    seed,
                    setNextCurrentDerivationPath(updatedAddressesCount)
            ).get(0);
        }
        nextReceivingAddress.setReceivingAddress(
            new ReceivingAddress(BigDecimal.ZERO, 0, address)
        );
    }

    public ObservableList<ReceivingAddress> getReceivingAddresses() {
        return receivingAddresses;
    }

    public boolean contains(String address) {
        return receivingAddressesMap.containsKey(address);
    }

    public ReceivingAddress get(String address) {
        return receivingAddressesMap.get(address);
    }

    public DerivationPath getCurrentDerivationPath() {
        return currentDerivationPath;
    }

    public DerivationPath setNextCurrentDerivationPath(int next) {
        currentDerivationPath = currentDerivationPath.next(next);
        return currentDerivationPath;
    }
}
