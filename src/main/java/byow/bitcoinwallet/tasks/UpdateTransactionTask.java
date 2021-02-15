package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.transaction.TransactionUpdater;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateTransactionTask extends Task<Void> {
    private final TransactionUpdater transactionUpdater;

    private final ReentrantLock reentrantLock;

    private final Object transaction;

    public UpdateTransactionTask(
        TransactionUpdater transactionUpdater,
        ReentrantLock reentrantLock,
        Object transaction
    ) {
        this.transactionUpdater = transactionUpdater;
        this.reentrantLock = reentrantLock;
        this.transaction = transaction;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            transactionUpdater.update(transaction);
        }
        return null;
    }
}
