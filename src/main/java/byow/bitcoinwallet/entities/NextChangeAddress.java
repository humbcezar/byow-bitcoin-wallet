package byow.bitcoinwallet.entities;

import org.springframework.stereotype.Component;

@Component
public class NextChangeAddress extends AbstractNextAddress {
    @Override
    public void setReceivingAddress(ReceivingAddress receivingAddress) {
        this.receivingAddress = receivingAddress;
    }
}
