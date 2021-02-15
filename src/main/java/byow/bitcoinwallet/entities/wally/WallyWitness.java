package byow.bitcoinwallet.entities.wally;

import java.io.ByteArrayOutputStream;

import static com.blockstream.libwally.Wally.*;
import static wf.bitcoin.krotjson.HexCoder.decode;

public class WallyWitness {
    private final Object witness;

    private byte[] publicKey;

    public WallyWitness(int itemsSize) {
        witness = tx_witness_stack_init(itemsSize);
    }

    public void addPublicKey(byte[] publicKey) {
        tx_witness_stack_add(witness, publicKey);
        this.publicKey = publicKey;
    }

    public void addDummySignature(int item) {
        tx_witness_stack_add_dummy(witness, item);
    }

    public void addSignature(byte[] signatureWithHashType) {
        tx_witness_stack_add(witness, signatureWithHashType);
    }

    public Object getWitness() {
        return witness;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] scriptCode() {
        ByteArrayOutputStream scriptCodeStream = new ByteArrayOutputStream();
        scriptCodeStream.writeBytes(decode("76a914"));
        scriptCodeStream.writeBytes(hash160(publicKey));
        scriptCodeStream.write(OP_EQUALVERIFY);
        scriptCodeStream.write(OP_CHECKSIG);
        return scriptCodeStream.toByteArray();
    }
}
