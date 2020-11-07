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
import static java.lang.Integer.min;

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

    public List<String> initializeReceivingAddresses(int numberOfAddresses, String seed, Date walletCreationDate) {
        List<String> addresses = addressSequentialGenerator.deriveAddresses(numberOfAddresses, seed, currentDerivationPath)
            .stream()
            .filter(address -> !receivingAddressesMap.containsKey(address))
            .peek(address -> {
                ReceivingAddress receivingAddress = new ReceivingAddress(BigDecimal.ZERO, -1, address);
                receivingAddressesMap.put(address, receivingAddress);
                receivingAddresses.add(receivingAddress);
            })
            .collect(Collectors.toList());
        if (!addresses.isEmpty()) {
            multiAddressesImporter.importMultiAddresses(walletCreationDate, addresses.toArray(new String[0]));
        }
        return addresses;
    }

    public int updateReceivingAddresses(List<String> addressList) {
        Map<String, List<ReceivingAddress>> collectedAddressMap = getUtxos(addressList)
                .stream()
                .map(utxo -> new ReceivingAddress(utxo.amount(), utxo.confirmations(), utxo.address()))
                .collect(Collectors.groupingBy(ReceivingAddress::getAddress));

        collectedAddressMap.forEach(
            (address, receivingAddresses) ->
                receivingAddresses.stream()
                    .filter(receivingAddress -> receivingAddressesMap.containsKey(receivingAddress.getAddress()))
                    .reduce(
                        (address1, address2) -> new ReceivingAddress(
                            new BigDecimal(address1.getBalance()).add(new BigDecimal(address2.getBalance())),
                            min(address1.getConfirmations(), address2.getConfirmations()),
                            address1.getAddress()
                        )
                    ).ifPresent(receivingAddress -> {
                        ReceivingAddress currentReceivingAddress = receivingAddressesMap.get(receivingAddress.getAddress());
                        currentReceivingAddress.setBalance(receivingAddress.getBalance());
                        currentReceivingAddress.setConfirmations(receivingAddress.getConfirmations());
                    })
        );

        return collectedAddressMap.size();
    }

    public void updateReceivingAddresses() {
        updateReceivingAddresses(
            receivingAddresses.stream()
                    .map(ReceivingAddress::getAddress)
                    .collect(Collectors.toList())
        );
    }

    private List<Unspent> getUtxos(List<String> addressList) {
        return bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
    }

    public void updateNextAddress(String address, int updatedAddressesCount, String seed, Date walletCreationDate) {
        if (updatedAddressesCount > 0) {
            address = addressSequentialGenerator.deriveAddresses(
                1,
                seed,
                setNextCurrentDerivationPath(updatedAddressesCount)
            ).get(0);
        }
        initializeReceivingAddresses(1, seed, walletCreationDate);
        nextReceivingAddress.setReceivingAddress(
            new ReceivingAddress(BigDecimal.ZERO, 0, address)
        );
        if (!getUtxos(List.of(nextReceivingAddress.getValue().getAddress())).isEmpty()) {
            updateNextAddress(address, 1, seed, walletCreationDate);
        }
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
