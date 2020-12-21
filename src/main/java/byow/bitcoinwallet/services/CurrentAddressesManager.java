package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Address;
import byow.bitcoinwallet.entities.NextAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static javafx.application.Platform.runLater;

abstract public class CurrentAddressesManager {
    private CurrentReceivingAddresses currentReceivingAddresses;

    private AddressSequentialGenerator addressSequentialGenerator;

    private MultiAddressesImporter multiAddressesImporter;

    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private UtxosGetter utxosGetter;

    @Autowired
    public CurrentAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter
    ) {
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.multiAddressesImporter = multiAddressesImporter;
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.utxosGetter = utxosGetter;
    }

    protected abstract void setNextAddress(ReceivingAddress address);

    protected abstract NextAddress getNextAddress();

    protected abstract DerivationPath getCurrentDerivationPath();

    protected abstract DerivationPath setNextCurrentDerivationPath(int initialAddressToMonitor);

    public abstract void clear();

    public List<String> initializeAddresses(int numberOfAddresses, String seed, Date walletCreationDate) {
        List<String> addresses = addressSequentialGenerator.deriveAddresses(numberOfAddresses, seed, getCurrentDerivationPath())
            .stream()
            .filter(address -> !currentReceivingAddresses.contains(address.getAddress()))
            .peek(address -> currentReceivingAddresses.add(
                ZERO,
                -1,
                address.getAddress(),
                address.getDerivationPath()
            ))
            .map(Address::getAddress)
            .collect(Collectors.toList());
        if (!addresses.isEmpty()) {
            multiAddressesImporter.importMultiAddresses(walletCreationDate, addresses.toArray(new String[0]));
        }
        return addresses;
    }

    public void update(String seed, Date walletCreationDate, int initialAddressToMonitor) {
        List<String> addressList = initializeAddresses(
            initialAddressToMonitor,
            seed,
            walletCreationDate
        );
        int updatedAddressesCount = currentReceivingAddressesUpdater.updateReceivingAddresses(addressList);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            setNextCurrentDerivationPath(initialAddressToMonitor);
            update(seed, walletCreationDate, initialAddressToMonitor);
            return;
        }
        updateNextAddress(addressList.get(0), updatedAddressesCount, seed, walletCreationDate);
    }

    public void updateNextAddress(String address, int updatedAddressesCount, String seed, Date walletCreationDate) {
        if (updatedAddressesCount > 0) {
            address = addressSequentialGenerator.deriveAddresses(
                1,
                seed,
                setNextCurrentDerivationPath(updatedAddressesCount)
            ).get(0).getAddress();
        }
        initializeAddresses(1, seed, walletCreationDate);
        ReceivingAddress nextAddress = new ReceivingAddress(ZERO, 0, address);
        if (!utxosGetter.getUtxos(List.of(address)).isEmpty()) {
            updateNextAddress(address, 1, seed, walletCreationDate);
            return;
        }
        runLater(() -> setNextAddress(nextAddress));
    }

}
