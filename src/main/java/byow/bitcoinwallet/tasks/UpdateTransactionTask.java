package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.transaction.TransactionUpdater;
import javafx.concurrent.Task;

import java.util.List;

public class UpdateTransactionTask extends Task<Void> {
    private final TransactionUpdater transactionUpdater;

    private final Object transaction;
    private final List<String> addresses;
    private final List<String> outputs;

    public UpdateTransactionTask(
        TransactionUpdater transactionUpdater,
        Object transaction,
        List<String> addresses,
        List<String> outputs
    ) {
        this.transactionUpdater = transactionUpdater;
        this.transaction = transaction;
        this.addresses = addresses;
        this.outputs = outputs;
    }

    @Override
    protected Void call() {
        transactionUpdater.update(transaction, addresses, outputs);
        return null;
    }
}
