package byow.bitcoinwallet.entities;

import javafx.beans.value.ObservableValueBase;
import org.springframework.stereotype.Component;

@Component
public class NextReceivingAddress extends ObservableValueBase<ReceivingAddress> {
    private ReceivingAddress receivingAddress;

    public synchronized void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
        this.fireValueChangedEvent();
    }

    @Override
    public synchronized ReceivingAddress getValue() {
        return receivingAddress;
    }
}
