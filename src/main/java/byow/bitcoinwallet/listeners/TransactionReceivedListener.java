package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.CurrentWalletManager;
import byow.bitcoinwallet.services.TransactionUpdater;
import byow.bitcoinwallet.services.TaskConfigurer;
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
    @Autowired
    private TransactionUpdater transactionUpdater;

    @Autowired
    private ReentrantLock reentrantLock;

    @Autowired
    private TaskConfigurer taskConfigurer;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        if (currentWalletManager.getCurrentWallet() != null) {
            new Thread(buildTask(event)).start();
        }
    }

    private Task<Void> buildTask(TransactionReceivedEvent event) {
        return taskConfigurer.configure(
            new UpdateTransactionTask(transactionUpdater, reentrantLock, event.getRawTransaction()),
            "Receiving transaction..."
        );
    }
}
