package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.wallet.CurrentWalletManager;
import javafx.concurrent.Task;

public class UpdateCurrentWalletTask extends Task<Void> {
    private final CurrentWalletManager currentWalletManager;

    private final Wallet wallet;

    private final CurrentTransactions currentTransactions;

    public UpdateCurrentWalletTask(
        CurrentWalletManager currentWalletManager,
        Wallet wallet,
        CurrentTransactions currentTransactions
    ) {
        this.currentWalletManager = currentWalletManager;
        this.wallet = wallet;
        this.currentTransactions = currentTransactions;
    }

    @Override
    protected Void call() {
        currentWalletManager.updateCurrentWallet(wallet);
        currentTransactions.update(wallet);
        return null;
    }
}
