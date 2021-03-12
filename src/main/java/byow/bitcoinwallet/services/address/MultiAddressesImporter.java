package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.util.*;

import static java.util.Objects.isNull;

@Component
@Lazy
public class MultiAddressesImporter {
    private final BitcoinJSONRPCClient bitcoindRpcClient;

    @Autowired
    public MultiAddressesImporter(BitcoinJSONRPCClient bitcoindRpcClient) {
        this.bitcoindRpcClient = bitcoindRpcClient;
    }

    public void importMultiAddresses(Date walletCreationDate, String label, String... addresses) {
        bitcoindRpcClient.query("importmulti", makeRequest(walletCreationDate, addresses, label));
    }

    private List<Object> makeRequest(Date walletCreationDate, String[] addresses, String label) {
        return new ArrayList<>(){
            {
                Arrays.stream(addresses).forEach(address -> add(new LinkedHashMap<>(){
                    {
                        put("scriptPubKey", new LinkedHashMap<String, String>() {
                            {
                                put("address", address);
                            }
                        });
                        put("timestamp", isNull(walletCreationDate) ? 0 : walletCreationDate.toInstant().getEpochSecond());
                        put("watchonly", true);
                        put("label", label);
                    }
                }));
            }
        };
    }
}
