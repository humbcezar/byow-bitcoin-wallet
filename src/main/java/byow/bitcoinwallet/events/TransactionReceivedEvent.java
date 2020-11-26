package byow.bitcoinwallet.events;

import org.springframework.context.ApplicationEvent;

public class TransactionReceivedEvent extends ApplicationEvent {
    private final Object transaction;

    public TransactionReceivedEvent(Object source, Object transaction) {
        super(source);
        this.transaction = transaction;
    }

    public Object getTransaction() {
        return transaction;
    }
}
