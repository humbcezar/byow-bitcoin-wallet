package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.WallyTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

public interface CoinSelector {
    WallyTransaction select(
        List<Unspent> utxos,
        BigDecimal target,
        BigDecimal feeRate,
        String seed,
        String toAddress,
        String changeAddress
    );
}
