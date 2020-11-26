package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.TransactionUpdater;
import javafx.concurrent.Task;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateTransactionTask extends Task<Void> {
    private TransactionUpdater transactionUpdater;

    private ReentrantLock reentrantLock;

    private Object transaction;

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
