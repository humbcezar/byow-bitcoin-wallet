package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

@Component
@Lazy
public class TransactionSender {
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    public void send(Transaction transaction) {
        bitcoindRpcClient.sendRawTransaction(transaction.toHex());
    }
}
