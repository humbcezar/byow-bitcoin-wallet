package byow.bitcoinwallet.entities;

import static com.blockstream.libwally.Wally.*;

public class WallyTransactionOutput {

    private byte[] scriptPubKey;

    private final Object output;

    private final long amount;

    private final String address;

    public WallyTransactionOutput(long amount, byte[] scriptPubKey, String address) {
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
        this.address = address;
        output = tx_output_init(amount, scriptPubKey);
    }

    public Object getOutput() {
        return output;
    }

    public long getAmount() {
        return amount;
    }

    public String getAddress() {
        return address;
    }
}
