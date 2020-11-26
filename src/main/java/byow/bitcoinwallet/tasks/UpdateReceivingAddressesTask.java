package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.CurrentAddressesManager;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateReceivingAddressesTask extends Task<Void> {
    private CurrentAddressesManager currentAddressesManager;

    private ReentrantLock reentrantLock;

    public UpdateReceivingAddressesTask(
            CurrentAddressesManager currentAddressesManager,
            ReentrantLock reentrantLock
    ) {
        this.currentAddressesManager = currentAddressesManager;
        this.reentrantLock = reentrantLock;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            currentAddressesManager.updateReceivingAddresses();
        }
        return null;
    }
}
