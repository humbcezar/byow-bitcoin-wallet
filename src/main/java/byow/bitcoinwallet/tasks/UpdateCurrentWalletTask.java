package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.CurrentWalletManager;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateCurrentWalletTask extends Task<Void> {
    private CurrentWalletManager currentWalletManager;

    private ReentrantLock reentrantLock;

    private Wallet wallet;

    public UpdateCurrentWalletTask(
            CurrentWalletManager currentWalletManager,
            ReentrantLock reentrantLock,
            Wallet wallet
    ) {
        this.currentWalletManager = currentWalletManager;
        this.reentrantLock = reentrantLock;
        this.wallet = wallet;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            currentWalletManager.updateCurrentWallet(wallet);
        }
        return null;
    }
}
