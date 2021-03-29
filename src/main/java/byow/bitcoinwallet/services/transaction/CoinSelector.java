package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.XPub;
import byow.bitcoinwallet.entities.wally.WallyTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface CoinSelector {
    WallyTransaction select(
        List<Unspent> utxos,
        BigDecimal target,
        BigDecimal feeRate,
        Set<XPub> xPubs,
        String toAddress,
        String changeAddress
    );
}
