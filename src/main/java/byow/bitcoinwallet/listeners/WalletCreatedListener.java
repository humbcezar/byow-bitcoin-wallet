package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.services.CurrentWalletManager;
import byow.bitcoinwallet.services.WalletsMenuManager;
import byow.bitcoinwallet.services.TaskConfigurer;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED;

@Lazy
@Component
public class WalletCreatedListener implements ApplicationListener<WalletCreatedEvent> {

    @Autowired
    WalletsMenuManager walletsMenuManager;

    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Autowired
    private ReentrantLock reentrantLock;

    @Autowired
    private TaskConfigurer taskConfigurer;

    @Override
    public void onApplicationEvent(WalletCreatedEvent event) {
        walletsMenuManager.addWallet(event.getWallet());
        new Thread(buildTask(event)).start();
    }

    private Task<Void> buildTask(WalletCreatedEvent event) {
        Task<Void> task = taskConfigurer.configure(
            new UpdateCurrentWalletTask(currentWalletManager, reentrantLock, event.getWallet()),
            "Loading wallet..."
        );
        task.addEventFilter(WORKER_STATE_SUCCEEDED, succeededEvent ->
            currentWalletManager.setWalletName(event.getWallet().getName())
        );
        return task;
    }
}
