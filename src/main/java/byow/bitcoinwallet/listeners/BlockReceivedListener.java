package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.BlockReceivedEvent;
import byow.bitcoinwallet.services.UtxosGetter;
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

import java.util.concurrent.ExecutorService;

@Component
@Lazy
public class BlockReceivedListener implements ApplicationListener<BlockReceivedEvent> {
    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final TaskConfigurer taskConfigurer;

    private final UtxosGetter utxosGetter;

    private final CurrentTransactions currentTransactions;

    private final CurrentWallet currentWallet;

    private final ExecutorService executorService;

    @Autowired
    public BlockReceivedListener(
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        TaskConfigurer taskConfigurer,
        UtxosGetter utxosGetter,
        CurrentTransactions currentTransactions,
        CurrentWallet currentWallet,
        ExecutorService executorService
    ) {
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.taskConfigurer = taskConfigurer;
        this.utxosGetter = utxosGetter;
        this.currentTransactions = currentTransactions;
        this.currentWallet = currentWallet;
        this.executorService = executorService;
    }

    @Override
    public void onApplicationEvent(BlockReceivedEvent event) {
        if (currentWallet.getCurrentWallet() != null) {
            executorService.submit(buildTask());
        }
    }

    private Task<Void> buildTask() {
        return taskConfigurer.configure(
            new UpdateReceivingAddressesTask(
                currentReceivingAddressesUpdater,
                utxosGetter,
                currentWallet.getCurrentWallet(),
                currentTransactions
            ),
            "Synchronizing new block..."
        );
    }
}
