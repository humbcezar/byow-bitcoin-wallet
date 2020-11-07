package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.TransactionUpdater;
import byow.bitcoinwallet.services.TaskConfigurer;
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

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        new Thread(buildTask(event)).start();
    }

    private Task<Void> buildTask(TransactionReceivedEvent event) {
        return taskConfigurer.configure(
            new Task<>() {
                @Override
                protected Void call() {
                    synchronized (reentrantLock) {
                        transactionUpdater.update(event.getRawTransaction());
                    }
                    return null;
                }
            },
            "Receiving transaction..."
        );
    }
}
