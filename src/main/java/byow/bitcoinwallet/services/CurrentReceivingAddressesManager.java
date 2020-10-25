package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.ReceivingAddress;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.*;

@Component
public class CurrentReceivingAddressesManager {
    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>(),
        receivingAddress -> new Observable[]{receivingAddress.balanceProperty(), receivingAddress.confirmationsProperty()}
    );

    private final LinkedHashMap<String, ReceivingAddress> receivingAddressesMap = new LinkedHashMap<>();

    public void clear() {
        receivingAddresses.clear();
        receivingAddressesMap.clear();
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

    public ObservableList<ReceivingAddress> getReceivingAddresses() {
        return receivingAddresses;
    }

    public boolean contains(String address) {
        return receivingAddressesMap.containsKey(address);
    }

    public ReceivingAddress get(String address) {
        return receivingAddressesMap.get(address);
    }
}
