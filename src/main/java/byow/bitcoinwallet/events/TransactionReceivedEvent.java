package byow.bitcoinwallet.events;

import org.springframework.context.ApplicationEvent;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

public class TransactionReceivedEvent extends ApplicationEvent {
    private final RawTransaction rawTransaction;

    public TransactionReceivedEvent(Object source, RawTransaction rawTransaction) {
        super(source);
        this.rawTransaction = rawTransaction;
    }

    public RawTransaction getRawTransaction() {
        return rawTransaction;
    }
}
