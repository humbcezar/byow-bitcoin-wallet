package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.SendTransactionService;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;

public class SendTransactionTask extends Task<Void> {
    private ReentrantLock reentrantLock;

    private SendTransactionService sendTransactionService;

    private TextField amountToSend;

    private TextField addressToSend;

    public SendTransactionTask(
        ReentrantLock reentrantLock,
        SendTransactionService sendTransactionService,
        TextField amountToSend,
        TextField addressToSend
    ) {
        this.reentrantLock = reentrantLock;
        this.sendTransactionService = sendTransactionService;
        this.amountToSend = amountToSend;
        this.addressToSend = addressToSend;
    }

    @Override
    protected Void call() throws Exception {
        synchronized (reentrantLock) {
            sendTransactionService.send(
                addressToSend.getText(),
                new BigDecimal(amountToSend.getText())
            );
        }
        return null;
    }
}
