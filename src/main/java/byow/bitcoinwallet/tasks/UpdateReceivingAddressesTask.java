package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.CurrentReceivingAddressesUpdater;
import byow.bitcoinwallet.services.CurrentTransactions;
import byow.bitcoinwallet.services.UtxosGetter;
import javafx.concurrent.Task;
import java.util.concurrent.locks.ReentrantLock;

import static javafx.application.Platform.runLater;

public class UpdateReceivingAddressesTask extends Task<Void> {
    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private ReentrantLock reentrantLock;

    private UtxosGetter utxosGetter;

    private Wallet wallet;

    private CurrentTransactions currentTransactions;

    public UpdateReceivingAddressesTask(
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        ReentrantLock reentrantLock,
        UtxosGetter utxosGetter,
        Wallet wallet,
        CurrentTransactions currentTransactions
    ) {
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.reentrantLock = reentrantLock;
        this.utxosGetter = utxosGetter;
        this.wallet = wallet;
        this.currentTransactions = currentTransactions;
    }

    @Override
    protected Void call() {
        synchronized (reentrantLock) {
            currentReceivingAddressesUpdater.updateReceivingAddresses(utxosGetter.getUtxos());
            runLater(() -> currentTransactions.update(wallet));
        }
        return null;
    }
}
