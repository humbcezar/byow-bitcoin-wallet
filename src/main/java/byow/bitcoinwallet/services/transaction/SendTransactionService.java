package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.Address;
import byow.bitcoinwallet.entities.TransactionInput;
import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionInputRepository;
import byow.bitcoinwallet.services.gui.CurrentWallet;
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

    private final TransactionSender transactionSender;

    private final TransactionSaver transactionSaver;

    private final CurrentWallet currentWallet;

    private final TransactionInputRepository transactionInputRepository;

    private final AddressRepository addressRepository;

    @Autowired
    public SendTransactionService(
        TransactionSender transactionSender,
        TransactionSaver transactionSaver,
        CurrentWallet currentWallet,
        TransactionInputRepository transactionInputRepository,
        AddressRepository addressRepository
    ) {
        this.transactionSender = transactionSender;
        this.transactionSaver = transactionSaver;
        this.currentWallet = currentWallet;
        this.transactionInputRepository = transactionInputRepository;
        this.addressRepository = addressRepository;
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
                    addressRepository.findByAddress(transactionInput.getAddress()).orElseGet(() ->
                        addressRepository.save(new Address(transactionInput.getAddress()))
                    ),
                    transactionInput.getAmountInSatoshis()
                )
            )).collect(Collectors.toSet());
        transactionSaver.save(txId, currentWallet.getCurrentWallet(), inputs, Set.of());
    }
}
