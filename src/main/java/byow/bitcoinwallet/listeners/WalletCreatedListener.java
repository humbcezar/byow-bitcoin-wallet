package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.gui.WalletsMenuManager;
import byow.bitcoinwallet.services.wallet.CurrentWalletManager;
import byow.bitcoinwallet.tasks.TaskConfigurer;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

import static javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED;

@Lazy
@Component
public class WalletCreatedListener implements ApplicationListener<WalletCreatedEvent> {

    private final WalletsMenuManager walletsMenuManager;

    private final CurrentWalletManager currentWalletManager;

    private final TaskConfigurer taskConfigurer;

    private final CurrentTransactions currentTransactions;

    private final CurrentWallet currentWallet;

    private final ExecutorService executorService;

    @Autowired
    public WalletCreatedListener(
        WalletsMenuManager walletsMenuManager,
        CurrentWalletManager currentWalletManager,
        TaskConfigurer taskConfigurer,
        CurrentTransactions currentTransactions,
        CurrentWallet currentWallet,
        ExecutorService executorService
    ) {
        this.walletsMenuManager = walletsMenuManager;
        this.currentWalletManager = currentWalletManager;
        this.taskConfigurer = taskConfigurer;
        this.currentTransactions = currentTransactions;
        this.currentWallet = currentWallet;
        this.executorService = executorService;
    }

    @Override
    public void onApplicationEvent(WalletCreatedEvent event) {
        walletsMenuManager.addWallet(event.getWallet());
        executorService.submit(buildTask(event));
    }

    private Task<Void> buildTask(WalletCreatedEvent event) {
        Task<Void> task = taskConfigurer.configure(
            new UpdateCurrentWalletTask(currentWalletManager, event.getWallet(), currentTransactions),
            "Loading wallet..."
        );
        task.addEventFilter(WORKER_STATE_SUCCEEDED, succeededEvent ->
            currentWallet.setWalletName(event.getWallet().getName())
        );
        return task;
    }
}
