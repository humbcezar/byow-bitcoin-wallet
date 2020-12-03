package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Transaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CoinSelector {
    Transaction select(
            List<Unspent> utxos,
            BigDecimal target,
            BigDecimal feeRate,
            Map<String, ReceivingAddress> receivingAddressMap,
            String seed,
            String toAddress,
            String changeAddress
    );
}
