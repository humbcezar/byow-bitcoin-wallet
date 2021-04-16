package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.transaction.TransactionUpdater;
import javafx.concurrent.Task;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateTransactionTask extends Task<Void> {
    private final TransactionUpdater transactionUpdater;

    private final ReentrantLock reentrantLock;

    private final Object transaction;
    private final List<String> addresses;
    private final List<String> outputs;

    public UpdateTransactionTask(
        TransactionUpdater transactionUpdater,
        ReentrantLock reentrantLock,
        Object transaction,
        List<String> addresses,
        List<String> outputs
    ) {
        this.transactionUpdater = transactionUpdater;
        this.reentrantLock = reentrantLock;
        this.transaction = transaction;
        this.addresses = addresses;
        this.outputs = outputs;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            transactionUpdater.update(transaction, addresses, outputs);
        }
        return null;
    }
}
