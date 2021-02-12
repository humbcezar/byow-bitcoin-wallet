package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.*;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
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
    private CurrentReceivingAddresses currentReceivingAddresses;

    private AddressSequentialGenerator addressSequentialGenerator;

    private MultiAddressesImporter multiAddressesImporter;

    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private UtxosGetter utxosGetter;

    private TransactionSaver transactionSaver;

    private TransactionOutputRepository transactionOutputRepository;

    private WalletRepository walletRepository;

    @Autowired
    public CurrentAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        TransactionSaver transactionSaver,
        TransactionOutputRepository transactionOutputRepository,
        WalletRepository walletRepository
    ) {
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.multiAddressesImporter = multiAddressesImporter;
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.utxosGetter = utxosGetter;
        this.transactionSaver = transactionSaver;
        this.transactionOutputRepository = transactionOutputRepository;
        this.walletRepository = walletRepository;
    }

    protected abstract void setNextAddress(ReceivingAddress address);

    protected abstract NextAddress getNextAddress();

    protected abstract DerivationPath getCurrentDerivationPath();

    protected abstract DerivationPath setNextCurrentDerivationPath(int next);

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

    public void update(Wallet wallet, int initialAddressToMonitor) {
        List<String> addressList = initializeAddresses(
            initialAddressToMonitor,
            wallet.getSeed(),
            wallet.getCreatedAt()
        );
        List<Unspent> utxos = utxosGetter.getUtxos(addressList);
        saveTransaction(wallet, utxos);
        int updatedAddressesCount = currentReceivingAddressesUpdater.updateReceivingAddresses(
            addressList,
            utxos
        );
        if (updatedAddressesCount >= initialAddressToMonitor) {
            setNextCurrentDerivationPath(initialAddressToMonitor);
            update(wallet, initialAddressToMonitor);
            return;
        }
        updateNextAddress(addressList.get(0), updatedAddressesCount, wallet.getSeed(), wallet.getCreatedAt());
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
                            .map(unspent -> new TransactionOutput(unspent.address(), btcToSatoshi(unspent.amount())))
                            .map(transactionOutput -> transactionOutputRepository.save(transactionOutput))
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
