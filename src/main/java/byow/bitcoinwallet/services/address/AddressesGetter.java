package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.util.Map;
import java.util.Set;

@Component
@Lazy
public class AddressesGetter {
    private final BitcoinJSONRPCClient bitcoindRpcClient;

    @Autowired
    public AddressesGetter(BitcoinJSONRPCClient bitcoindRpcClient) {
        this.bitcoindRpcClient = bitcoindRpcClient;
    }

    public Set<String> getAddressesByLabel(String label) {
        try {
            Map<String, Object> addressesMap = (Map<String, Object>) bitcoindRpcClient.query("getaddressesbylabel", label);
            return addressesMap.keySet();
        } catch (Exception ignored) {
        }
        return Set.of();
    }
}
