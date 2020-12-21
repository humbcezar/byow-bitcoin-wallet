package byow.bitcoinwallet.entities;

import org.springframework.stereotype.Component;

@Component
public class NextChangeAddress implements NextAddress {
    private ReceivingAddress receivingAddress;

    @Override
    public void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
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
