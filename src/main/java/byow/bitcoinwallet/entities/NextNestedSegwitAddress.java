package byow.bitcoinwallet.entities;

import javafx.beans.value.ObservableValueBase;
import org.springframework.stereotype.Component;

@Component
public class NextNestedSegwitAddress extends ObservableValueBase<ReceivingAddress> implements NextAddress {
    private ReceivingAddress receivingAddress;

    @Override
    public void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
        this.fireValueChangedEvent();
    }

    @Override
    public ReceivingAddress getValue() {
        return receivingAddress;
    }

    @Override
    public boolean equalAddress(String address) {
        return receivingAddress.getAddress().equals(address);
    }
}
