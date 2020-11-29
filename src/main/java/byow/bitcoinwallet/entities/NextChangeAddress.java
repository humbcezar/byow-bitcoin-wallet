package byow.bitcoinwallet.entities;

import org.springframework.stereotype.Component;

@Component
public class NextChangeAddress {
    private ReceivingAddress receivingAddress;

    public ReceivingAddress getReceivingAddress() {
        return receivingAddress;
    }

    public void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
    }

    public boolean equalAddress(String address) {
        return receivingAddress.getAddress().equals(address);
    }
}
