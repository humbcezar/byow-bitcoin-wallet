package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.CurrentReceivingAddressesUpdater;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateReceivingAddressesTask extends Task<Void> {
    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private ReentrantLock reentrantLock;

    public UpdateReceivingAddressesTask(
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        ReentrantLock reentrantLock
    ) {
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.reentrantLock = reentrantLock;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            currentReceivingAddressesUpdater.updateReceivingAddresses();
        }
        return null;
    }
}
