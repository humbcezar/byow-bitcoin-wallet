package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.*;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

@Component
@Lazy
public class CurrentTransactions {
    private ObservableList<TransactionRow> currentTransactions = new ObservableListWrapper<>(new LinkedList<>());

    private WalletRepository walletRepository;

    private TransactionGetter transactionGetter;

    @Autowired
    public CurrentTransactions(
        WalletRepository walletRepository,
        TransactionGetter transactionGetter
    ) {
        this.walletRepository = walletRepository;
        this.transactionGetter = transactionGetter;
    }

    public void clear() {
        currentTransactions.clear();
    }

    public void update(Wallet wallet) {
        wallet = walletRepository.findById(wallet.getId()).orElseThrow();
        currentTransactions.setAll(parse(wallet.getTransactions()));
    }

    private List<TransactionRow> parse(List<Transaction> transactions) {
        return transactions.stream()
            .map(transaction -> new TransactionRow(
                transaction.getTxId(),
                calculateBalance(transaction),
                getConfirmations(transaction.getTxId()),
                transaction.getCreatedAt().toString()
            ))
            .distinct()
            .collect(Collectors.toList());
    }

    private int getConfirmations(String txId) {
        try {
            BitcoindRpcClient.RawTransaction transaction = transactionGetter.get(txId);
            return transaction.confirmations();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String calculateBalance(Transaction transaction) {
        return valueOf(
            ((transaction.getTransactionOutputs()
                .stream()
                .map(TransactionOutput::getSatoshis)
                .reduce(Long::sum)
                .orElse(0L)
                .doubleValue())
            - (transaction.getTransactionInputs()
            .stream()
            .map(TransactionInput::getSatoshis)
            .reduce(Long::sum)
            .orElse(0L)
            .doubleValue())) / 100_000_000
        );
    }

    public ObservableList<TransactionRow> get() {
        return currentTransactions;
    }

}
