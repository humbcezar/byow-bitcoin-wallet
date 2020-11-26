package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;

@Component
@Lazy
public class RescanAborter {
    private BitcoinJSONRPCClient bitcoindRpcClient;

    @Autowired
    public RescanAborter(BitcoinJSONRPCClient bitcoindRpcClient) {
        this.bitcoindRpcClient = bitcoindRpcClient;
    }

    public void abortRescan() {
        bitcoindRpcClient.query("abortrescan");
    }
}
