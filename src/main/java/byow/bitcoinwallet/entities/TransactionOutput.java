package byow.bitcoinwallet.entities;

import static com.blockstream.libwally.Wally.*;

public class TransactionOutput {

    private byte[] scriptPubKey;

    private final Object output;

    private final long amount;

    public TransactionOutput(long amount, byte[] scriptPubKey) {
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
        output = tx_output_init(amount, scriptPubKey);
    }

    public Object getOutput() {
        return output;
    }

    public long getAmount() {
        return amount;
    }
}
