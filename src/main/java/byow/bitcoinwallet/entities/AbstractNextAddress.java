package byow.bitcoinwallet.entities;

import javafx.beans.value.ObservableValueBase;

public abstract class AbstractNextAddress extends ObservableValueBase<ReceivingAddress> implements NextAddress {
    protected ReceivingAddress receivingAddress;

    @Override
    public ReceivingAddress getValue() {
        return receivingAddress;
    }

    @Override
    public boolean equalAddress(String address) {
        return receivingAddress.getAddress().equals(address);
    }
}
