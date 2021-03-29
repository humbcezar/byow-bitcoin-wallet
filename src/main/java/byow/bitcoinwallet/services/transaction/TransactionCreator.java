package byow.bitcoinwallet.services.transaction;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.UtxosGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

@Component
@Lazy
public class TransactionCreator {
    private final UtxosGetter utxosGetter;

    private final FeeEstimator feeEstimator;

    private final CoinSelector coinSelector;

    private final CurrentWallet currentWallet;

    private final NextChangeAddress nextChangeAddress;

    @Autowired
    public TransactionCreator(
        UtxosGetter utxosGetter,
        FeeEstimator feeEstimator,
        CoinSelector coinSelector,
        CurrentWallet currentWallet,
        NextChangeAddress nextChangeAddress
    ) {
        this.utxosGetter = utxosGetter;
        this.feeEstimator = feeEstimator;
        this.coinSelector = coinSelector;
        this.currentWallet = currentWallet;
        this.nextChangeAddress = nextChangeAddress;
    }

    public WallyTransaction create(String addressToSend, BigDecimal amountToSend) {
        List<Unspent> utxos = utxosGetter.getUtxos();
        BigDecimal feeRate = feeEstimator.estimate();
        return coinSelector.select(
            utxos,
            amountToSend,
            feeRate,
            currentWallet.getCurrentWallet().getxPubs(),
            addressToSend,
            nextChangeAddress.getValue().getAddress()
        );
    }
}
