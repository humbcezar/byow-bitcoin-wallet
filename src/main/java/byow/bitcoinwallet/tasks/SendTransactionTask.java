package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Transaction;
import byow.bitcoinwallet.services.SendTransactionService;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class SendTransactionTask extends Task<Void> {
    private ReentrantLock reentrantLock;

    private SendTransactionService sendTransactionService;

    private Transaction transaction;

    public SendTransactionTask(
        ReentrantLock reentrantLock,
        SendTransactionService sendTransactionService,
        Transaction transaction
    ) {
        this.reentrantLock = reentrantLock;
        this.sendTransactionService = sendTransactionService;
        this.transaction = transaction;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            sendTransactionService.signAndSend(transaction);
        }
        return null;
    }
}
