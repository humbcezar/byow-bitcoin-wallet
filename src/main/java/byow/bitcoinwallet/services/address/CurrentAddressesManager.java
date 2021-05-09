package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.*;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.UtxosGetter;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.transaction.TransactionSaver;
import org.springframework.beans.factory.annotation.Autowired;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.utils.SatoshiUtils.btcToSatoshi;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static javafx.application.Platform.runLater;

abstract public class CurrentAddressesManager {
    private final CurrentReceivingAddresses currentReceivingAddresses;

    private final AddressSequentialGenerator addressSequentialGenerator;

    private final MultiAddressesImporter multiAddressesImporter;

    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final UtxosGetter utxosGetter;

    private final TransactionSaver transactionSaver;

    private final TransactionOutputRepository transactionOutputRepository;

    private final WalletRepository walletRepository;

    private final AddressRepository addressRepository;

    private final AddressesGetter addressesGetter;

    protected DerivationPath currentDerivationPath;

    protected NextAddress nextAddress;

    @Autowired
    public CurrentAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        TransactionSaver transactionSaver,
        TransactionOutputRepository transactionOutputRepository,
        WalletRepository walletRepository,
        AddressRepository addressRepository,
        AddressesGetter addressesGetter
    ) {
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.multiAddressesImporter = multiAddressesImporter;
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.utxosGetter = utxosGetter;
        this.transactionSaver = transactionSaver;
        this.transactionOutputRepository = transactionOutputRepository;
        this.walletRepository = walletRepository;
        this.addressRepository = addressRepository;
        this.addressesGetter = addressesGetter;
    }

    protected void setNextAddress(ReceivingAddress address) {
        nextAddress.setReceivingAddress(address);
    }

    public NextAddress getNextAddress() {
        return nextAddress;
    }

    protected DerivationPath getCurrentDerivationPath() {
        return currentDerivationPath;
    }

    protected DerivationPath setNextCurrentDerivationPath(int next) {
        currentDerivationPath = currentDerivationPath.next(next);
        return currentDerivationPath;
    }

    public abstract void clear();

    public abstract XPubTypes getXPubType();

    public List<String> initializeAddresses(int numberOfAddresses, Wallet wallet) {
        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                numberOfAddresses, wallet.getXPub(getXPubType()).getKey(), getCurrentDerivationPath()
            )
            .stream()
            .filter(address -> !currentReceivingAddresses.contains(address.getAddress()))
            .peek(address -> currentReceivingAddresses.add(
                ZERO,
                -1,
                address.getAddress(),
                address.getDerivationPath()
            ))
            .map(AddressPath::getAddress)
            .collect(Collectors.toList());

        List<String> unimportedAddresses = getUnimportedAddresses(addresses, wallet.getName());
        if (!unimportedAddresses.isEmpty()) {
            importAddresses(wallet, unimportedAddresses);
        }
        return addresses;
    }

    private void importAddresses(Wallet wallet, List<String> unimportedAddresses) {
        Date date = wallet.getLastImportedAt() == null ? wallet.getCreatedAt() : wallet.getLastImportedAt();
        wallet.setLastImportedAt(new Date());
        multiAddressesImporter.importMultiAddresses(date, wallet.getName(), unimportedAddresses.toArray(new String[0]));
        walletRepository.save(wallet);
    }

    public void update(Wallet wallet, int numberOfAddresses) {
        List<String> addressList = initializeAddresses(
            numberOfAddresses,
            wallet
        );
        List<Unspent> utxos = utxosGetter.getUtxos(addressList);
        saveTransaction(wallet, utxos);
        int updatedAddressesCount = currentReceivingAddressesUpdater.updateReceivingAddresses(
            addressList,
            utxos
        );
        if (updatedAddressesCount >= numberOfAddresses) {
            setNextCurrentDerivationPath(numberOfAddresses);
            update(wallet, numberOfAddresses);
            return;
        }
        updateNextAddress(addressList.get(0), updatedAddressesCount, wallet);
    }

    @Transactional
    private void saveTransaction(Wallet wallet, List<Unspent> utxos) {
        utxos.stream()
            .collect(groupingBy(Unspent::txid))
            .forEach((txId, unspents) -> {
                if (!walletContainsTransaction(wallet, txId)) {
                    transactionSaver.save(
                        txId,
                        wallet,
                        Set.of(),
                        unspents.stream()
                            .map(unspent -> new TransactionOutput(
                                addressRepository.findByAddress(unspent.address()).orElseGet(() ->
                                    addressRepository.save(new Address(unspent.address()))
                                ),
                                btcToSatoshi(unspent.amount())
                            ))
                            .map(transactionOutputRepository::save)
                            .collect(toSet()));
                }
            });
    }

    private boolean walletContainsTransaction(Wallet wallet, String txId) {
        return walletRepository.findByName(wallet.getName())
            .getTransactions()
            .stream()
            .anyMatch(transaction -> transaction.getTxId().equals(txId));
    }

    public void updateNextAddress(String address, int updatedAddressesCount, Wallet wallet) {
        if (updatedAddressesCount > 0) {
            address = addressSequentialGenerator.deriveAddresses(
                1,
                wallet.getXPub(getXPubType()).getKey(),
                setNextCurrentDerivationPath(updatedAddressesCount)
            ).get(0).getAddress();
        }
        initializeAddresses(1, wallet);
        ReceivingAddress nextReceivingAddress = new ReceivingAddress(ZERO, 0, address);
        if (addressRepository.existsByAddress(address)) {
            updateNextAddress(address, 1, wallet);
            return;
        }
        runLater(() -> setNextAddress(nextReceivingAddress));
    }

    private List<String> getUnimportedAddresses(List<String> addresses, String walletName) {
        if (addresses.isEmpty()) {
            return List.of();
        }
        Set<String> importedAddresses = addressesGetter.getAddressesByLabel(walletName);
        return addresses.stream()
            .filter(address -> !importedAddresses.contains(address))
            .collect(Collectors.toList());
    }

}