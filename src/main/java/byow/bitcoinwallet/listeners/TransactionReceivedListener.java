package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.transaction.TransactionUpdater;
import byow.bitcoinwallet.tasks.TaskConfigurer;
import byow.bitcoinwallet.tasks.UpdateTransactionTask;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
@Lazy
public class TransactionReceivedListener implements ApplicationListener<TransactionReceivedEvent> {
    private final TransactionUpdater transactionUpdater;

    private final ReentrantLock reentrantLock;

    private final TaskConfigurer taskConfigurer;

    private final CurrentWallet currentWallet;

    @Autowired
    public TransactionReceivedListener(
        TransactionUpdater transactionUpdater,
        ReentrantLock reentrantLock,
        TaskConfigurer taskConfigurer,
        CurrentWallet currentWallet
    ) {
        this.transactionUpdater = transactionUpdater;
        this.reentrantLock = reentrantLock;
        this.taskConfigurer = taskConfigurer;
        this.currentWallet = currentWallet;
    }

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        if (currentWallet.getCurrentWallet() != null) {
            new Thread(buildTask(event)).start();
        }
    }

    private Task<Void> buildTask(TransactionReceivedEvent event) {
        return taskConfigurer.configure(
            new UpdateTransactionTask(transactionUpdater, reentrantLock, event.getTransaction()),
            "Receiving transaction..."
        );
    }
}
