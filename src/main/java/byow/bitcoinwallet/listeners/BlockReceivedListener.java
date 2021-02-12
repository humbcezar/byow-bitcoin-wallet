package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.BlockReceivedEvent;
import byow.bitcoinwallet.services.*;
import byow.bitcoinwallet.tasks.UpdateReceivingAddressesTask;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
@Lazy
public class BlockReceivedListener implements ApplicationListener<BlockReceivedEvent> {
    @Autowired
    private CurrentWalletManager currentWalletManager;

    @Autowired
    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    @Autowired
    private ReentrantLock reentrantLock;

    @Autowired
    private TaskConfigurer taskConfigurer;

    @Autowired
    private UtxosGetter utxosGetter;

    @Autowired
    private CurrentTransactions currentTransactions;

    @Override
    public void onApplicationEvent(BlockReceivedEvent event) {
        if (currentWalletManager.getCurrentWallet() != null) {
            new Thread(buildTask()).start();
        }
    }

    private Task<Void> buildTask() {
        return taskConfigurer.configure(
            new UpdateReceivingAddressesTask(
                currentReceivingAddressesUpdater,
                reentrantLock,
                utxosGetter,
                currentWalletManager.getCurrentWallet(),
                currentTransactions
            ),
            "Synchronizing new block..."
        );
    }
}
