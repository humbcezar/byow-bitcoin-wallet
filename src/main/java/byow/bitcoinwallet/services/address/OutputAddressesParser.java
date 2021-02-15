package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.blockstream.libwally.Wally.*;
import static com.blockstream.libwally.Wally.tx_get_output_script;
import static java.util.stream.IntStream.range;

@Component
@Lazy
public class OutputAddressesParser {
    private final int networkVersion;

    private final String addressPrefix;

    public OutputAddressesParser(
        @Qualifier("networkVersion") int networkVersion,
        @Qualifier("addressPrefix") String addressPrefix
    ) {
        this.networkVersion = networkVersion;
        this.addressPrefix = addressPrefix;
    }

    public List<String> parseOutputAddresses(Object transaction) {
        return range(0, tx_get_num_outputs(transaction)).mapToObj(i -> {
            try {
                if (scriptpubkey_get_type(tx_get_output_script(transaction, i)) == WALLY_SCRIPT_TYPE_P2SH) {
                    return scriptpubkey_to_address(tx_get_output_script(transaction, i), networkVersion);
                }
                return addr_segwit_from_bytes(tx_get_output_script(transaction, i), addressPrefix, 0);
            } catch (IllegalArgumentException ignored) {
                return "";
            }
        }).collect(Collectors.toList());
    }
}
