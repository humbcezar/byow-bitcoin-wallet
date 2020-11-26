package byow.bitcoinwallet.entities;

import static com.blockstream.libwally.Wally.*;

public class TransactionOutput {

    private final byte[] scriptPubKey;

    private final Object output;

    private final long amount;

    public TransactionOutput(String address, String addressPrefix, long amount) {
        scriptPubKey = addr_segwit_to_bytes(address, addressPrefix, 0);
        output = tx_output_init(amount, scriptPubKey);
        this.amount = amount;
    }

    public Object getOutput() {
        return output;
    }

    public long getAmount() {
        return amount;
    }
}
