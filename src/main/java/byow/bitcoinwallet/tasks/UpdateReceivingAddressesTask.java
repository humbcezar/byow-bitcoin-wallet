package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateReceivingAddressesTask extends Task<Void> {
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private ReentrantLock reentrantLock;

    public UpdateReceivingAddressesTask(
            CurrentReceivingAddressesManager currentReceivingAddressesManager,
            ReentrantLock reentrantLock
    ) {
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        this.reentrantLock = reentrantLock;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            currentReceivingAddressesManager.updateReceivingAddresses();
        }
        return null;
    }
}
