package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.TransactionUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class TransactionReceivedListener implements ApplicationListener<TransactionReceivedEvent> {
    @Autowired
    private TransactionUpdater transactionUpdater;

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        transactionUpdater.update(event.getRawTransaction());
    }
}
