package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.BlockReceivedEvent;
import byow.bitcoinwallet.services.*;
import byow.bitcoinwallet.services.address.CurrentReceivingAddressesUpdater;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.tasks.TaskConfigurer;
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
    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final ReentrantLock reentrantLock;

    private final TaskConfigurer taskConfigurer;

    private final UtxosGetter utxosGetter;

    private final CurrentTransactions currentTransactions;

    private final CurrentWallet currentWallet;

    @Autowired
    public BlockReceivedListener(
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        ReentrantLock reentrantLock,
        TaskConfigurer taskConfigurer,
        UtxosGetter utxosGetter,
        CurrentTransactions currentTransactions,
        CurrentWallet currentWallet
    ) {
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.reentrantLock = reentrantLock;
        this.taskConfigurer = taskConfigurer;
        this.utxosGetter = utxosGetter;
        this.currentTransactions = currentTransactions;
        this.currentWallet = currentWallet;
    }

    @Override
    public void onApplicationEvent(BlockReceivedEvent event) {
        if (currentWallet.getCurrentWallet() != null) {
            new Thread(buildTask()).start();
        }
    }

    private Task<Void> buildTask() {
        return taskConfigurer.configure(
            new UpdateReceivingAddressesTask(
                currentReceivingAddressesUpdater,
                reentrantLock,
                utxosGetter,
                currentWallet.getCurrentWallet(),
                currentTransactions
            ),
            "Synchronizing new block..."
        );
    }
}
