package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.WallyTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

@Component
@Lazy
public class TransactionCreator {
    private UtxosGetter utxosGetter;

    private FeeEstimator feeEstimator;

    private CoinSelector coinSelector;

    private CurrentWalletManager currentWalletManager;

    private NextChangeAddress nextChangeAddress;

    @Autowired
    public TransactionCreator(
        UtxosGetter utxosGetter,
        FeeEstimator feeEstimator,
        CoinSelector coinSelector,
        CurrentWalletManager currentWalletManager,
        NextChangeAddress nextChangeAddress
    ) {
        this.utxosGetter = utxosGetter;
        this.feeEstimator = feeEstimator;
        this.coinSelector = coinSelector;
        this.currentWalletManager = currentWalletManager;
        this.nextChangeAddress = nextChangeAddress;
    }

    public WallyTransaction create(String addressToSend, BigDecimal amountToSend) {
        List<Unspent> utxos = utxosGetter.getUtxos();
        BigDecimal feeRate = feeEstimator.estimate();
        return coinSelector.select(
            utxos,
            amountToSend,
            feeRate,
            currentWalletManager.getCurrentWallet().getSeed(),
            addressToSend,
            nextChangeAddress.getValue().getAddress()
        );
    }
}
