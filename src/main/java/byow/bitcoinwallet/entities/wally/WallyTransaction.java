package byow.bitcoinwallet.entities.wally;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.blockstream.libwally.Wally.*;
import static java.util.Collections.addAll;

public class WallyTransaction {
    private final Object tx;

    private List<WallyTransactionOutput> outputs = new ArrayList<>();

    private List<WallyTransactionInput> inputs = new ArrayList<>();

    private long feeRateInSatoshisPerByte;

    private long totalFeeInSatoshis;

    private long intendedTotalFeeInSatoshis;

    public WallyTransaction(int numberOfInputs, int numberOfOutputs) {
        tx = tx_init(2, 0, numberOfInputs, numberOfOutputs);
    }

    public void addOutput(WallyTransactionOutput...txOutputs) {
        addAll(outputs, txOutputs);
        List.of(txOutputs).forEach(output -> tx_add_output(tx, output.getOutput()));
    }

    public void addInput(WallyTransactionInput transactionInput) {
        inputs.add(transactionInput);
        tx_add_input(tx, transactionInput.getInput());
    }

    public void sign(int inputIndex) {
        WallyTransactionInput input = inputs.get(inputIndex);
        byte[] sigHash = tx_get_btc_signature_hash(
            tx,
            inputIndex,
            input.getWitness().scriptCode(),
            input.getAmountInSatoshis(),
            WALLY_SIGHASH_ALL,
            WALLY_TX_FLAG_USE_WITNESS,
            null
        );
        byte[] signature = ec_sig_to_der(
            ec_sig_normalize(
                ec_sig_from_bytes(
                    input.getPrivateKey(), sigHash, EC_FLAG_ECDSA
                )
            )
        );
        byte[] signatureWithHashType = appendSigHashType(signature);
        WallyWitness signedWitness = new WallyWitness(2);
        signedWitness.addSignature(signatureWithHashType);
        signedWitness.addPublicKey(input.getWitness().getPublicKey());

        tx_set_input_witness(tx, inputIndex, signedWitness.getWitness());
        input.setWitness(signedWitness);
    }

    private byte[] appendSigHashType(byte[] signature) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.writeBytes(signature);
        byteArrayOutputStream.write(1);
        return byteArrayOutputStream.toByteArray();
    }

    public String toHex() {
        return tx_to_hex(tx, WALLY_TX_FLAG_USE_WITNESS);
    }

    public int getInputCount() {
        return inputs.size();
    }

    public int getOutputCount() {
        return tx_get_num_outputs(tx);
    }

    public long vSize() {
        return tx_get_vsize(tx);
    }

    public void removeOutput(int i) {
        tx_remove_output(tx, i);
        outputs.remove(i);
    }

    public WallyTransactionInput getInput(int index) {
        return inputs.get(index);
    }

    public WallyTransactionOutput getOutput(int index) {
        return outputs.get(index);
    }

    public void setFeeRateInSatoshisPerByte(long feeRateInSatoshisPerByte) {
        this.feeRateInSatoshisPerByte = feeRateInSatoshisPerByte;
    }

    public void setTotalFeeInSatoshis(long totalFeeInSatoshis) {
        this.totalFeeInSatoshis = totalFeeInSatoshis;
    }

    public long getFeeRateInSatoshisPerByte() {
        return feeRateInSatoshisPerByte;
    }

    public long getTotalFeeInSatoshis() {
        return totalFeeInSatoshis;
    }

    public void setIntendedTotalFeeInSatoshis(long intendedTotalFeeInSatoshis) {
        this.intendedTotalFeeInSatoshis = intendedTotalFeeInSatoshis;
    }

    public long getIntendedTotalFeeInSatoshis() {
        return intendedTotalFeeInSatoshis;
    }

    public List<WallyTransactionInput> getInputs() {
        return inputs;
    }

    public List<WallyTransactionOutput> getOutputs() {
        return outputs;
    }
}
