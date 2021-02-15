package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

@Component
@Lazy
public class TransactionSender {
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    public String send(WallyTransaction transaction) {
        return bitcoindRpcClient.sendRawTransaction(transaction.toHex());
    }
}
