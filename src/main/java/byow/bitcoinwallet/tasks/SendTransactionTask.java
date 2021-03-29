package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.services.transaction.SendTransactionService;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class SendTransactionTask extends Task<Void> {
    private ReentrantLock reentrantLock;

    private SendTransactionService sendTransactionService;

    private WallyTransaction transaction;

    private String seed;

    public SendTransactionTask(
        ReentrantLock reentrantLock,
        SendTransactionService sendTransactionService,
        WallyTransaction transaction,
        String seed
    ) {
        this.reentrantLock = reentrantLock;
        this.sendTransactionService = sendTransactionService;
        this.transaction = transaction;
        this.seed = seed;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            sendTransactionService.signAndSend(transaction, seed);
        }
        return null;
    }
}
