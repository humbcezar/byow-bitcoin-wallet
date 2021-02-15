package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.blockstream.libwally.Wally.*;
import static com.blockstream.libwally.Wally.addr_segwit_from_bytes;
import static java.util.stream.IntStream.range;
import static wf.bitcoin.krotjson.HexCoder.decode;

@Component
@Lazy
public class InputAddressesParser {
    private final int nestedAddressVersion;

    private final String addressPrefix;

    public InputAddressesParser(
        @Qualifier("nestedAddressVersion") int nestedAddressVersion,
        @Qualifier("addressPrefix") String addressPrefix
    ) {
        this.nestedAddressVersion = nestedAddressVersion;
        this.addressPrefix = addressPrefix;
    }

    public List<String> parseInputAddresses(Object transaction) {
        List<String> inputAddresses = new ArrayList<>();
        if (!tx_is_coinbase(transaction)) {
            int numInputs = tx_get_num_inputs(transaction);
            inputAddresses = range(0, numInputs).mapToObj(i -> {
                byte[] publicKey = tx_get_input_witness(transaction, i, 1);
                if (isNestedSegwitInput(transaction, i)) {
                    return buildNestedSegwitAddress(publicKey);
                }
                byte[] witness = witness_program_from_bytes(publicKey, WALLY_SCRIPT_HASH160);
                return addr_segwit_from_bytes(witness, addressPrefix, 0);
            }).collect(Collectors.toList());
        }

        return inputAddresses;
    }

    private boolean isNestedSegwitInput(Object transaction, long inputIndex) {
        byte[] script = tx_get_input_script(transaction, inputIndex);
        return script.length > 0;
    }

    private String buildNestedSegwitAddress(byte[] publicKey) {
        ByteArrayOutputStream redeemScriptStream = new ByteArrayOutputStream();
        redeemScriptStream.writeBytes(decode("0014"));
        redeemScriptStream.writeBytes(hash160(publicKey));
        byte[] redeemScript = redeemScriptStream.toByteArray();

        ByteArrayOutputStream addressStream = new ByteArrayOutputStream();
        addressStream.write(nestedAddressVersion);
        addressStream.writeBytes(hash160(redeemScript));
        return base58check_from_bytes(addressStream.toByteArray());
    }
}
