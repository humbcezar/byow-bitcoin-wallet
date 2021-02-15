package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.wallet.CurrentWalletManager;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateCurrentWalletTask extends Task<Void> {
    private CurrentWalletManager currentWalletManager;

    private ReentrantLock reentrantLock;

    private Wallet wallet;

    private CurrentTransactions currentTransactions;

    public UpdateCurrentWalletTask(
        CurrentWalletManager currentWalletManager,
        ReentrantLock reentrantLock,
        Wallet wallet,
        CurrentTransactions currentTransactions
    ) {
        this.currentWalletManager = currentWalletManager;
        this.reentrantLock = reentrantLock;
        this.wallet = wallet;
        this.currentTransactions = currentTransactions;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            currentWalletManager.updateCurrentWallet(wallet);
            currentTransactions.update(wallet);
        }
        return null;
    }
}
