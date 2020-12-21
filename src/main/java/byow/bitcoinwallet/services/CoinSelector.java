package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Transaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

public interface CoinSelector {
    Transaction select(
        List<Unspent> utxos,
        BigDecimal target,
        BigDecimal feeRate,
        String seed,
        String toAddress,
        String changeAddress
    );
}
