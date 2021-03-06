package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.Address;
import byow.bitcoinwallet.entities.TransactionOutput;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.address.AddressUpdater;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.utils.HexUtils.revertEndianess;
import static com.blockstream.libwally.Wally.tx_get_output_satoshi;
import static com.blockstream.libwally.Wally.tx_get_txid;
import static java.util.stream.IntStream.range;
import static wf.bitcoin.krotjson.HexCoder.encode;

@Component
@Lazy
public class TransactionUpdater {
    private final CurrentReceivingAddresses currentReceivingAddresses;

    private final CurrentWallet currentWallet;

    private final TransactionSaver transactionSaver;

    private final TransactionOutputRepository transactionOutputRepository;

    private final CurrentTransactions currentTransactions;

    private final AddressUpdater addressUpdater;

    private final AddressRepository addressRepository;

    private final WalletRepository walletRepository;

    @Autowired
    public TransactionUpdater(
        CurrentReceivingAddresses currentReceivingAddresses,
        CurrentWallet currentWallet,
        TransactionSaver transactionSaver,
        TransactionOutputRepository transactionOutputRepository,
        CurrentTransactions currentTransactions,
        AddressUpdater addressUpdater,
        AddressRepository addressRepository,
        WalletRepository walletRepository
    ) {
        this.currentReceivingAddresses = currentReceivingAddresses;
        this.currentWallet = currentWallet;
        this.transactionSaver = transactionSaver;
        this.transactionOutputRepository = transactionOutputRepository;
        this.currentTransactions = currentTransactions;
        this.addressUpdater = addressUpdater;
        this.addressRepository = addressRepository;
        this.walletRepository = walletRepository;
    }

    public void update(Object transaction, List<String> addresses, List<String> outputs) {
        addressUpdater.update(addresses.stream(), currentWallet.getCurrentWallet());
        updateTransactions(transaction, outputs);
    }

    private void updateTransactions(Object transaction, List<String> outputs) {
        Wallet wallet = currentWallet.getCurrentWallet().isWatchOnly() ? currentWallet.getCurrentWallet().getParent() : currentWallet.getCurrentWallet();
        saveTransaction(transaction, outputs, wallet);
        currentTransactions.update(wallet);
    }

    @Transactional
    private void saveTransaction(Object transaction, List<String> outputs, Wallet wallet) {
        String txId = revertEndianess(encode(tx_get_txid(transaction)));
        if (walletContainsTransaction(wallet, txId)) {
            return;
        }

        Set<TransactionOutput> transactionOutputs = range(0, outputs.size())
            .filter(index -> currentReceivingAddresses.contains(outputs.get(index)))
            .mapToObj(index -> {
                TransactionOutput transactionOutput = new TransactionOutput(
                    addressRepository.findByAddress(outputs.get(index)).orElseGet(() ->
                        addressRepository.save(new Address(outputs.get(index)))
                    ),
                    tx_get_output_satoshi(transaction, index)
                );
                return transactionOutputRepository.save(transactionOutput);
            }).collect(Collectors.toSet());

        if (!transactionOutputs.isEmpty()) {
            transactionSaver.save(
                txId,
                wallet,
                Set.of(),
                transactionOutputs
            );
        }
    }

    private boolean walletContainsTransaction(Wallet wallet, String txId) {
        return walletRepository.findByName(wallet.getName())
            .getTransactions()
            .stream()
            .anyMatch(transaction -> transaction.getTxId().equals(txId) && !transaction.getTransactionOutputs().isEmpty());
    }
}
