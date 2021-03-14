package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.address.CurrentReceivingAddressesUpdater;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.UtxosGetter;
import javafx.concurrent.Task;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateReceivingAddressesTask extends Task<Void> {
    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final ReentrantLock reentrantLock;

    private final UtxosGetter utxosGetter;

    private final Wallet wallet;

    private final CurrentTransactions currentTransactions;

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
            currentTransactions.update(wallet);
        }
        return null;
    }
}
