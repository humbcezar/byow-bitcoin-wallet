package byow.bitcoinwallet.entities;

import javafx.beans.value.ObservableValueBase;
import org.springframework.stereotype.Component;

@Component
public class NextReceivingAddress extends ObservableValueBase<ReceivingAddress> {
    private ReceivingAddress receivingAddress;

    public void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
        this.fireValueChangedEvent();
    }

    @Override
    public ReceivingAddress getValue() {
        return receivingAddress;
    }

    public boolean equalAddress(String address) {
        return receivingAddress.getAddress().equals(address);
    }
}
