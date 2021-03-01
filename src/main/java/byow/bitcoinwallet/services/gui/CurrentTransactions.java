package byow.bitcoinwallet.services.gui;

import byow.bitcoinwallet.entities.*;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.transaction.TransactionGetter;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.UNNECESSARY;
import static javafx.application.Platform.runLater;

@Component
@Lazy
public class CurrentTransactions {
    private final ObservableList<TransactionRow> currentTransactions = new ObservableListWrapper<>(new LinkedList<>());

    private final WalletRepository walletRepository;

    private final TransactionGetter transactionGetter;

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
        runLater(
            () -> currentTransactions.setAll(parse(
                walletRepository.findById(wallet.getId())
                    .orElseThrow()
                    .getTransactions()
            ))
        );
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
         return valueOf((transaction.getTransactionOutputs()
                .stream()
                .map(TransactionOutput::getSatoshis)
                .reduce(Long::sum)
                .orElse(0L))
                - (transaction.getTransactionInputs()
                .stream()
                .map(TransactionInput::getSatoshis)
                .reduce(Long::sum)
                .orElse(0L))
            ).divide(valueOf(100_000_000), 8, UNNECESSARY)
            .toString();
    }

    public ObservableList<TransactionRow> get() {
        return currentTransactions;
    }

}
