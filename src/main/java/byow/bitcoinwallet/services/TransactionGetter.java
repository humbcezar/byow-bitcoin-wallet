package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;

@Component
public class TransactionGetter {
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    public RawTransaction get(String txId) {
        return bitcoindRpcClient.getRawTransaction(txId);
    }
}
