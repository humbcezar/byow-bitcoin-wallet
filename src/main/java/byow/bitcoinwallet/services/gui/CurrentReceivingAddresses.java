package byow.bitcoinwallet.services.gui;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.address.DerivationPath;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;

@Component
@Lazy
public class CurrentReceivingAddresses {
    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>(),
        receivingAddress -> new Observable[]{receivingAddress.balanceProperty(), receivingAddress.confirmationsProperty()}
    );

    private final Map<String, ReceivingAddress> receivingAddressesMap = new ConcurrentHashMap<>();

    public void clear() {
        receivingAddresses.clear();
        receivingAddressesMap.clear();
    }

    public void add(BigDecimal balance, int confirmations, String address, DerivationPath derivationPath) {
        ReceivingAddress receivingAddress = new ReceivingAddress(
            balance,
            confirmations,
            address,
            derivationPath
        );
        receivingAddressesMap.put(address, receivingAddress);
        runLater(() -> receivingAddresses.add(receivingAddress));
    }

    public void updateReceivingAddress(String address, String balance, int confirmations) {
        ReceivingAddress currentReceivingAddress = receivingAddressesMap.get(address);
        currentReceivingAddress.setBalance(balance);
        currentReceivingAddress.setConfirmations(confirmations);
    }

    public boolean contains(String address) {
        return receivingAddressesMap.containsKey(address);
    }

    public List<String> getAddresses() {
        return receivingAddresses.stream()
            .map(ReceivingAddress::getAddress)
            .collect(Collectors.toList());
    }

    public ObservableList<ReceivingAddress> getReceivingAddresses() {
        return receivingAddresses;
    }

    public ReceivingAddress getReceivingAddress(String address) {
        return receivingAddressesMap.get(address);
    }
}
