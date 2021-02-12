package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.TransactionInput;
import byow.bitcoinwallet.entities.WallyTransaction;
import byow.bitcoinwallet.repositories.TransactionInputRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

@Component
@Lazy
public class SendTransactionService {

    private TransactionSender transactionSender;

    private TransactionSaver transactionSaver;

    private CurrentWalletManager currentWalletManager;

    private TransactionInputRepository transactionInputRepository;

    @Autowired
    public SendTransactionService(
        TransactionSender transactionSender,
        TransactionSaver transactionSaver,
        CurrentWalletManager currentWalletManager,
        TransactionInputRepository transactionInputRepository
    ) {
        this.transactionSender = transactionSender;
        this.transactionSaver = transactionSaver;
        this.currentWalletManager = currentWalletManager;
        this.transactionInputRepository = transactionInputRepository;
    }

    public void signAndSend(WallyTransaction transaction) {
        range(0, transaction.getInputCount()).forEach(transaction::sign);
        String txId = transactionSender.send(transaction);
        saveTransaction(transaction, txId);
    }

    @Transactional
    private void saveTransaction(WallyTransaction transaction, String txId) {
        Set<TransactionInput> inputs = transaction.getInputs()
            .stream()
            .map(transactionInput -> transactionInputRepository.save(
                new TransactionInput(
                    transactionInput.getAddress(),
                    transactionInput.getAmountInSatoshis()
                )
            )).collect(Collectors.toSet());
        transactionSaver.save(txId, currentWalletManager.getCurrentWallet(), inputs, Set.of());
    }
}
