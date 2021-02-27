package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoinRPCException;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

@Component
@Lazy
public class LoadNodeWallet {
    @Autowired
    private BitcoindRpcClient bitcoinJSONRPCClient;

    public void createOrLoadNodeWallet() {
        try {
            ((BitcoinJSONRPCClient) bitcoinJSONRPCClient).query("createwallet", "default");
        } catch (BitcoinRPCException exception) {
            if(exception.getRPCError().getCode() == -4) {
                loadNodeWallet();
            }
        }
    }

    private void loadNodeWallet() {
        try {
            ((BitcoinJSONRPCClient)bitcoinJSONRPCClient).query("loadwallet", "default");
        } catch (BitcoinRPCException ignored) {
        }
    }
}
