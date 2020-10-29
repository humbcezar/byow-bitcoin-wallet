package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Component
public class CurrentReceivingAddressesManager {

    private AddressSequentialGenerator addressSequentialGenerator;

    private NextReceivingAddress nextReceivingAddress;

    private MultiAddressesImporter multiAddressesImporter;

    private BitcoindRpcClient bitcoindRpcClient;

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>(),
            receivingAddress -> new Observable[]{receivingAddress.balanceProperty(), receivingAddress.confirmationsProperty()}
    );

    private final Map<String, ReceivingAddress> receivingAddressesMap = new ConcurrentHashMap<>();

    private DerivationPath currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;

    @Autowired
    public CurrentReceivingAddressesManager(
            AddressSequentialGenerator addressSequentialGenerator,
            NextReceivingAddress nextReceivingAddress,
            MultiAddressesImporter multiAddressesImporter,
            BitcoindRpcClient bitcoindRpcClient
    ) {
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.nextReceivingAddress = nextReceivingAddress;
        this.multiAddressesImporter = multiAddressesImporter;
        this.bitcoindRpcClient = bitcoindRpcClient;
    }

    public void clear() {
        receivingAddresses.clear();
        receivingAddressesMap.clear();
        currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;
        nextReceivingAddress.setReceivingAddress(
                new ReceivingAddress(BigDecimal.ZERO, 0, "")
        );
    }

    public List<String> initializeReceivingAddresses(int numberOfAddresses, String seed) {
        return addressSequentialGenerator.deriveAddresses(numberOfAddresses, seed, currentDerivationPath)
            .stream()
            .filter(address -> !receivingAddressesMap.containsKey(address))
            .peek(address -> {
                ReceivingAddress receivingAddress = new ReceivingAddress(BigDecimal.ZERO, -1, address);
                receivingAddressesMap.put(address, receivingAddress);
                receivingAddresses.add(receivingAddress);
            })
            .collect(Collectors.toList());
    }

    public int updateReceivingAddresses(List<String> addressList, Date walletCreationDate) {
        List<Unspent> utxos = getUtxos(addressList, walletCreationDate);
        Set<ReceivingAddress> changeSet = new HashSet<>();
        utxos.forEach(utxo -> updateReceivingAddress(utxo, changeSet));
        return changeSet.size();
    }

    private List<Unspent> getUtxos(List<String> addressList, Date walletCreationDate) {
        multiAddressesImporter.importMultiAddresses(walletCreationDate, addressList.toArray(new String[0]));
        return bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
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
