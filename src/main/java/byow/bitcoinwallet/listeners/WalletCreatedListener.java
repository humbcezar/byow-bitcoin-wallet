package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.wallet.CurrentWalletManager;
import byow.bitcoinwallet.services.wallet.WalletsMenuManager;
import byow.bitcoinwallet.tasks.TaskConfigurer;
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

    private final WalletsMenuManager walletsMenuManager;

    private final CurrentWalletManager currentWalletManager;

    private final ReentrantLock reentrantLock;

    private final TaskConfigurer taskConfigurer;

    private final CurrentTransactions currentTransactions;

    private final CurrentWallet currentWallet;

    @Autowired
    public WalletCreatedListener(
        WalletsMenuManager walletsMenuManager,
        CurrentWalletManager currentWalletManager,
        ReentrantLock reentrantLock,
        TaskConfigurer taskConfigurer,
        CurrentTransactions currentTransactions,
        CurrentWallet currentWallet
    ) {
        this.walletsMenuManager = walletsMenuManager;
        this.currentWalletManager = currentWalletManager;
        this.reentrantLock = reentrantLock;
        this.taskConfigurer = taskConfigurer;
        this.currentTransactions = currentTransactions;
        this.currentWallet = currentWallet;
    }

    @Override
    public void onApplicationEvent(WalletCreatedEvent event) {
        walletsMenuManager.addWallet(event.getWallet());
        new Thread(buildTask(event)).start();
    }

    private Task<Void> buildTask(WalletCreatedEvent event) {
        Task<Void> task = taskConfigurer.configure(
            new UpdateCurrentWalletTask(currentWalletManager, reentrantLock, event.getWallet(), currentTransactions),
            "Loading wallet..."
        );
        task.addEventFilter(WORKER_STATE_SUCCEEDED, succeededEvent ->
            currentWallet.setWalletName(event.getWallet().getName())
        );
        return task;
    }
}
