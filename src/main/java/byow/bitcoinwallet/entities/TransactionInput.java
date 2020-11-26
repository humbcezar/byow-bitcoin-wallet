package byow.bitcoinwallet.entities;

import static com.blockstream.libwally.Wally.tx_input_init;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;

public class TransactionInput {
    private final Object input;

    private final byte[] privateKey;

    private Witness witness;

    private final long amountInSatoshis;

    private final String txId;

    public TransactionInput(
        String txid,
        int vout,
        long amountInSatoshis,
        byte[] privateKey,
        long nSequence,
        byte[] scriptSig,
        Witness witness
    ) {
        this.txId = txid;
        this.witness = witness;
        input = tx_input_init(
            parseHexBinary(toLittleEndian(txid)),
            vout,
            nSequence,
            scriptSig,
            witness.getWitness()
        );
        this.amountInSatoshis = amountInSatoshis;
        this.privateKey = privateKey;
    }

    private String toLittleEndian(String txid) {
        StringBuilder  result = new StringBuilder();
        for (int i = 0; i <= txid.length() - 2; i = i + 2) {
            result.append(new StringBuilder(txid.substring(i, i + 2)).reverse());
        }
        return result.reverse().toString();
    }

    public Witness getWitness() {
        return witness;
    }

    public long getAmountInSatoshis() {
        return amountInSatoshis;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setWitness(Witness signedWitness) {
        this.witness = signedWitness;
    }

    public Object getInput() {
        return input;
    }

    public String getTxId() {
        return txId;
    }

}
