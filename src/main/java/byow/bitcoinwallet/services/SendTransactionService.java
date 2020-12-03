package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static java.util.stream.IntStream.range;

@Component
@Lazy
public class SendTransactionService {

    private TransactionSender transactionSender;

    @Autowired
    public SendTransactionService(
        TransactionSender transactionSender
    ) {
        this.transactionSender = transactionSender;
    }

    public void signAndSend(Transaction transaction) {
        range(0, transaction.getInputCount()).forEach(transaction::sign);
        transactionSender.send(transaction);
    }
}
