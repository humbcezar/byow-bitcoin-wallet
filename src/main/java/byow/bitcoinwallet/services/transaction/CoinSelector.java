package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
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
