package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.TransactionUpdater;
import javafx.concurrent.Task;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateTransactionTask extends Task<Void> {
    private TransactionUpdater transactionUpdater;

    private ReentrantLock reentrantLock;

    private RawTransaction rawTransaction;

    public UpdateTransactionTask(
            TransactionUpdater transactionUpdater,
            ReentrantLock reentrantLock,
            RawTransaction rawTransaction
    ) {
        this.transactionUpdater = transactionUpdater;
        this.reentrantLock = reentrantLock;
        this.rawTransaction = rawTransaction;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            transactionUpdater.update(rawTransaction);
        }
        return null;
    }
}
