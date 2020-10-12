package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class MultiAddressesImporter {
    private BitcoinJSONRPCClient bitcoindRpcClient;

    @Autowired
    public MultiAddressesImporter(BitcoinJSONRPCClient bitcoindRpcClient) {
        this.bitcoindRpcClient = bitcoindRpcClient;
    }

    public void importMultiAddresses(String... addresses) {
        bitcoindRpcClient.query("importmulti", makeRequest(addresses));
    }

    private List<Object> makeRequest(String[] addresses) {
        return new ArrayList<>(){
            {
                Arrays.stream(addresses).forEach(address -> {
                    add(new LinkedHashMap<>(){
                        {
                            put("scriptPubKey", new LinkedHashMap<String, String>() {
                                {
                                    put("address", address);
                                }
                            });
                            put("timestamp", 0);
                            put("watchonly", true);
                        }
                    });
                });
            }
        };
    }
}
