package byow.bitcoinwallet.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@SpringBootTest
public class MultiAddressesImporterTest {

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @MockBean
    private BitcoinJSONRPCClient bitcoindRpcClient;

    @Test
    public void importMulti() {
        MultiAddressesImporter multiAddressesImporter = new MultiAddressesImporter(bitcoindRpcClient);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        String address1 = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);
        String address2 = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next());
        String address3 = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next().next());
        multiAddressesImporter.importMultiAddresses(address1, address2, address3);
        Mockito.verify(bitcoindRpcClient).query("importmulti", expectedRequest(address1, address2, address3));
    }

    private List<Object> expectedRequest(String... addresses) {
        return new ArrayList<>(){
            {
                add(new LinkedHashMap<>(){
                    {
                        put("scriptPubKey", new LinkedHashMap<String, String>() {
                            {
                                put("address", addresses[0]);
                            }
                        });
                        put("timestamp", 0);
                        put("watchonly", true);
                    }
                });
                add(new LinkedHashMap<>(){
                    {
                        put("scriptPubKey", new LinkedHashMap<String, String>() {
                            {
                                put("address", addresses[1]);
                            }
                        });
                        put("timestamp", 0);
                        put("watchonly", true);
                    }
                });
                add(new LinkedHashMap<>(){
                    {
                        put("scriptPubKey", new LinkedHashMap<String, String>() {
                            {
                                put("address", addresses[2]);
                            }
                        });
                        put("timestamp", 0);
                        put("watchonly", true);
                    }
                });
            }
        };

    }
}
