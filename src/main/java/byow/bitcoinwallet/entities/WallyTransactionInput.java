package byow.bitcoinwallet.entities;

import static byow.bitcoinwallet.utils.HexUtils.revertEndianess;
import static com.blockstream.libwally.Wally.tx_input_init;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;

public class WallyTransactionInput {
    private final Object input;

    private final byte[] privateKey;

    private byte[] scriptSig;

    private WallyWitness witness;

    private String address;

    private final long amountInSatoshis;

    private final String txId;

    public WallyTransactionInput(
        String txid,
        int vout,
        long amountInSatoshis,
        byte[] privateKey,
        long nSequence,
        byte[] scriptSig,
        WallyWitness witness,
        String address
    ) {
        this.txId = txid;
        this.scriptSig = scriptSig;
        this.witness = witness;
        this.address = address;
        input = tx_input_init(
            parseHexBinary(revertEndianess(txid)),
            vout,
            nSequence,
            scriptSig,
            witness.getWitness()
        );
        this.amountInSatoshis = amountInSatoshis;
        this.privateKey = privateKey;
    }

    public WallyWitness getWitness() {
        return witness;
    }

    public long getAmountInSatoshis() {
        return amountInSatoshis;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setWitness(WallyWitness signedWitness) {
        this.witness = signedWitness;
    }

    public Object getInput() {
        return input;
    }

    public String getTxId() {
        return txId;
    }

    public byte[] getScriptSig() {
        return scriptSig;
    }

    public void setScriptSig(byte[] scriptSig) {
        this.scriptSig = scriptSig;
    }

    public String getAddress() {
        return address;
    }
}
