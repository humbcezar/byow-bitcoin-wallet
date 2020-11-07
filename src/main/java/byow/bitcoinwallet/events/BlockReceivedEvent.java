package byow.bitcoinwallet.events;

import org.springframework.context.ApplicationEvent;

public class BlockReceivedEvent extends ApplicationEvent {
    public BlockReceivedEvent(Object source) {
        super(source);
    }
}
