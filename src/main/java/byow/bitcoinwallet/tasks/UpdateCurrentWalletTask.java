package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.wallet.CurrentWalletManager;
import javafx.concurrent.Task;

import java.util.concurrent.locks.ReentrantLock;

public class UpdateCurrentWalletTask extends Task<Void> {
    private final CurrentWalletManager currentWalletManager;

    private final ReentrantLock reentrantLock;

    private final Wallet wallet;

    private final CurrentTransactions currentTransactions;

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
