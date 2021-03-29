package byow.bitcoinwallet.entities.wally;

import byow.bitcoinwallet.services.address.DerivationPath;

import static byow.bitcoinwallet.utils.HexUtils.revertEndianess;
import static com.blockstream.libwally.Wally.tx_input_init;
import static javax.xml.bind.DatatypeConverter.parseHexBinary;

public class WallyTransactionInput {
    private final Object input;

    private byte[] scriptSig;

    private WallyWitness witness;

    private final String address;

    private final DerivationPath derivationPath;

    private final long amountInSatoshis;

    private final String txId;

    public WallyTransactionInput(
        String txid,
        int vout,
        long amountInSatoshis,
        long nSequence,
        byte[] scriptSig,
        WallyWitness witness,
        String address,
        DerivationPath derivationPath
    ) {
        this.txId = txid;
        this.scriptSig = scriptSig;
        this.witness = witness;
        this.address = address;
        this.derivationPath = derivationPath;
        input = tx_input_init(
            parseHexBinary(revertEndianess(txid)),
            vout,
            nSequence,
            scriptSig,
            witness.getWitness()
        );
        this.amountInSatoshis = amountInSatoshis;
    }

    public WallyWitness getWitness() {
        return witness;
    }

    public long getAmountInSatoshis() {
        return amountInSatoshis;
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

    public DerivationPath getDerivationPath() {
        return derivationPath;
    }
}
