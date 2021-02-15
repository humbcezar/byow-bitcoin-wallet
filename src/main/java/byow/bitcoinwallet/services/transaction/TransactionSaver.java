package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.Transaction;
import byow.bitcoinwallet.entities.TransactionInput;
import byow.bitcoinwallet.entities.TransactionOutput;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Component
public class TransactionSaver {
    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction save(String txId, Wallet wallet, Set<TransactionInput> inputs, Set<TransactionOutput> outputs) {
        Transaction transaction = transactionRepository.findByTxIdAndWallets_Id(txId, wallet.getId()).orElseGet(
            () -> new Transaction(txId, new Date())
        );
        if (!transaction.getWallets().contains(wallet)) {
            transaction.appendWallet(wallet);
        }
        if (!inputs.isEmpty()) {
            transaction.setTransactionInputs(inputs);
        }
        if (!outputs.isEmpty()) {
            transaction.setTransactionOutputs(outputs);
        }
        return transactionRepository.save(transaction);
    }
}
