package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.services.transaction.SendTransactionService;
import javafx.concurrent.Task;

public class SendTransactionTask extends Task<Void> {
    private final SendTransactionService sendTransactionService;

    private final WallyTransaction transaction;

    private final String seed;

    public SendTransactionTask(
        SendTransactionService sendTransactionService,
        WallyTransaction transaction,
        String seed
    ) {
        this.sendTransactionService = sendTransactionService;
        this.transaction = transaction;
        this.seed = seed;
    }

    @Override
    protected Void call() {
        sendTransactionService.signAndSend(transaction, seed);
        return null;
    }
}
