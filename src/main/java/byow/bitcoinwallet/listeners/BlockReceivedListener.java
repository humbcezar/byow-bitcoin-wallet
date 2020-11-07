package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.BlockReceivedEvent;
import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import byow.bitcoinwallet.services.CurrentWalletManager;
import byow.bitcoinwallet.services.TaskConfigurer;
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
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    @Autowired
    private ReentrantLock reentrantLock;

    @Autowired
    private TaskConfigurer taskConfigurer;

    @Override
    public void onApplicationEvent(BlockReceivedEvent event) {
        if (currentWalletManager.getCurrentWallet() != null) {
            new Thread(buildTask()).start();
        }
    }

    private Task<Void> buildTask() {
        return taskConfigurer.configure(
            new Task<>() {
                @Override
                protected Void call() {
                    synchronized (reentrantLock) {
                        currentReceivingAddressesManager.updateReceivingAddresses();
                    }
                    return null;
                }
            },
            "Synchronizing new block..."
        );
    }
}
