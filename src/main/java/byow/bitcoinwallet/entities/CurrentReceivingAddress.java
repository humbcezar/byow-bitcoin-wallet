package byow.bitcoinwallet.entities;

import javafx.beans.value.ObservableValueBase;

public class CurrentReceivingAddress extends ObservableValueBase<ReceivingAddress> {
    private ReceivingAddress receivingAddress;

    public void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
        this.fireValueChangedEvent();
    }

    @Override
    public ReceivingAddress getValue() {
        return receivingAddress;
    }
}
