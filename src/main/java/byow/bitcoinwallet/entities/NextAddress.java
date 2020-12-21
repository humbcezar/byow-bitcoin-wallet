package byow.bitcoinwallet.entities;

public interface NextAddress {
    void setReceivingAddress(ReceivingAddress receivingAddress);

    ReceivingAddress getValue();

    boolean equalAddress(String address);
}
