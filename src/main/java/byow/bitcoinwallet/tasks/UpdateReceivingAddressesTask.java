package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.UtxosGetter;
import byow.bitcoinwallet.services.address.CurrentReceivingAddressesUpdater;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import javafx.concurrent.Task;

public class UpdateReceivingAddressesTask extends Task<Void> {
    private final CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    private final UtxosGetter utxosGetter;

    private final Wallet wallet;

    private final CurrentTransactions currentTransactions;

    public UpdateReceivingAddressesTask(
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        Wallet wallet,
        CurrentTransactions currentTransactions
    ) {
        this.currentReceivingAddressesUpdater = currentReceivingAddressesUpdater;
        this.utxosGetter = utxosGetter;
        this.wallet = wallet;
        this.currentTransactions = currentTransactions;
    }

    @Override
    protected Void call() {
        currentReceivingAddressesUpdater.updateReceivingAddresses(utxosGetter.getUtxos());
        currentTransactions.update(wallet);
        return null;
    }
}
