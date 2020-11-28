package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Address;
import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_CHANGE_PATH;
import static java.lang.Integer.min;
import static java.math.BigDecimal.ZERO;
import static javafx.application.Platform.runLater;

@Component
@Lazy
public class CurrentAddressesManager {

    private AddressSequentialGenerator addressSequentialGenerator;

    private NextReceivingAddress nextReceivingAddress;

    private NextChangeAddress nextChangeAddress;

    private MultiAddressesImporter multiAddressesImporter;

    private BitcoindRpcClient bitcoindRpcClient;

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>(),
        receivingAddress -> new Observable[]{receivingAddress.balanceProperty(), receivingAddress.confirmationsProperty()}
    );

    private final Map<String, ReceivingAddress> receivingAddressesMap = new ConcurrentHashMap<>();

    private DerivationPath currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;

    private DerivationPath currentChangeDerivationPath = FIRST_BIP84_CHANGE_PATH;

    @Autowired
    public CurrentAddressesManager(
        AddressSequentialGenerator addressSequentialGenerator,
        NextReceivingAddress nextReceivingAddress,
        MultiAddressesImporter multiAddressesImporter,
        BitcoindRpcClient bitcoindRpcClient,
        NextChangeAddress nextChangeAddress
    ) {
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.nextReceivingAddress = nextReceivingAddress;
        this.multiAddressesImporter = multiAddressesImporter;
        this.bitcoindRpcClient = bitcoindRpcClient;
        this.nextChangeAddress = nextChangeAddress;
    }

    public void clear() {
        receivingAddresses.clear();
        receivingAddressesMap.clear();
        currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;
        currentChangeDerivationPath = FIRST_BIP84_CHANGE_PATH;
        nextReceivingAddress.setReceivingAddress(
            new ReceivingAddress(ZERO, 0, "")
        );
        nextChangeAddress.setReceivingAddress(
            new ReceivingAddress(ZERO, 0, "")
        );
    }

    public List<String> initializeReceivingAddresses(int numberOfAddresses, String seed, Date walletCreationDate) {
        return initializeAddresses(numberOfAddresses, seed, walletCreationDate, currentDerivationPath);
    }

    public List<String> initializeChangeAddresses(int numberOfAddresses, String seed, Date walletCreationDate) {
        return initializeAddresses(numberOfAddresses, seed, walletCreationDate, currentChangeDerivationPath);
    }

    private List<String> initializeAddresses(int numberOfAddresses, String seed, Date walletCreationDate, DerivationPath derivationPath) {
        List<String> addresses = addressSequentialGenerator.deriveAddresses(numberOfAddresses, seed, derivationPath)
            .stream()
            .filter(address -> !receivingAddressesMap.containsKey(address.getAddress()))
            .peek(address -> {
                ReceivingAddress receivingAddress = new ReceivingAddress(
                        ZERO,
                        -1,
                        address.getAddress(),
                        address.getDerivationPath()
                );
                receivingAddressesMap.put(address.getAddress(), receivingAddress);
                receivingAddresses.add(receivingAddress);
            })
            .map(Address::getAddress)
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
        addressList.stream()
            .filter(address -> isSpent(collectedAddressMap, address))
            .forEach(address -> collectedAddressMap.put(address, List.of(new ReceivingAddress(ZERO, -1, address))));

        collectedAddressMap.forEach(
            (address, addresses) ->
                addresses.stream()
                    .filter(receivingAddress -> receivingAddressesMap.containsKey(receivingAddress.getAddress()))
                    .reduce(
                        (address1, address2) -> new ReceivingAddress(
                            address1.getBigDecimalBalance().add(address2.getBigDecimalBalance()),
                            min(address1.getConfirmations(), address2.getConfirmations()),
                            address1.getAddress()
                        )
                    ).ifPresent(receivingAddress -> {
                        ReceivingAddress currentReceivingAddress = receivingAddressesMap.get(receivingAddress.getAddress());
                        runLater(() -> {
                            currentReceivingAddress.setBalance(receivingAddress.getBalance());
                            currentReceivingAddress.setConfirmations(receivingAddress.getConfirmations());
                        });
                    })
        );
        return collectedAddressMap.size();
    }

    private boolean isSpent(Map<String, List<ReceivingAddress>> collectedAddressMap, String address) {
        return !collectedAddressMap.containsKey(address) &&
                receivingAddressesMap.containsKey(address) &&
                receivingAddressesMap.get(address).getBigDecimalBalance().compareTo(ZERO) > 0;
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

    public void updateNextReceivingAddress(String address, int updatedAddressesCount, String seed, Date walletCreationDate) {
        if (updatedAddressesCount > 0) {
            address = addressSequentialGenerator.deriveAddresses(
                1,
                seed,
                setNextCurrentDerivationPath(updatedAddressesCount)
            ).get(0).getAddress();
        }
        initializeReceivingAddresses(1, seed, walletCreationDate);
        ReceivingAddress nextAddress = new ReceivingAddress(ZERO, 0, address);
        if (!getUtxos(List.of(address)).isEmpty()) {
            updateNextReceivingAddress(address, 1, seed, walletCreationDate);
            return;
        }
        runLater(() -> nextReceivingAddress.setReceivingAddress(nextAddress));
    }

    public void updateNextChangeAddress(String address, int updatedAddressesCount, String seed, Date walletCreationDate) {
        if (updatedAddressesCount > 0) {
            address = addressSequentialGenerator.deriveAddresses(
                1,
                seed,
                setNextCurrentChangeDerivationPath(updatedAddressesCount)
            ).get(0).getAddress();
        }
        initializeChangeAddresses(1, seed, walletCreationDate);
        ReceivingAddress nextAddress = new ReceivingAddress(ZERO, 0, address);
        if (!getUtxos(List.of(address)).isEmpty()) {
            updateNextChangeAddress(address, 1, seed, walletCreationDate);
            return;
        }
        runLater(() -> nextChangeAddress.setReceivingAddress(nextAddress));
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

    public DerivationPath setNextCurrentChangeDerivationPath(int next) {
        currentChangeDerivationPath = currentChangeDerivationPath.next(next);
        return currentChangeDerivationPath;
    }

    public List<Unspent> getUtxos() {
        return getUtxos(
            receivingAddresses.stream()
                .map(ReceivingAddress::getAddress)
                .collect(Collectors.toList())
        );
    }

    public Map<String, ReceivingAddress> getReceivingAddressesMap() {
        return receivingAddressesMap;
    }
}
