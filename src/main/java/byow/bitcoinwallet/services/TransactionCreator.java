package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

@Component
@Lazy
public class TransactionCreator {

    private CurrentAddressesManager currentAddressesManager;

    private FeeEstimator feeEstimator;

    private CoinSelector coinSelector;

    private CurrentWalletManager currentWalletManager;

    private NextChangeAddress nextChangeAddress;

    @Autowired
    public TransactionCreator(
        CurrentAddressesManager currentAddressesManager,
        FeeEstimator feeEstimator,
        CoinSelector coinSelector,
        CurrentWalletManager currentWalletManager,
        NextChangeAddress nextChangeAddress
    ) {
        this.currentAddressesManager = currentAddressesManager;
        this.feeEstimator = feeEstimator;
        this.coinSelector = coinSelector;
        this.currentWalletManager = currentWalletManager;
        this.nextChangeAddress = nextChangeAddress;
    }

    public Transaction create(String addressToSend, BigDecimal amountToSend) {
        List<Unspent> utxos = currentAddressesManager.getUtxos();
        BigDecimal feeRate = feeEstimator.estimate();
        return coinSelector.select(
            utxos,
            amountToSend,
            feeRate,
            currentAddressesManager.getReceivingAddressesMap(),
            currentWalletManager.getCurrentWallet().getSeed(),
            addressToSend,
            nextChangeAddress.getReceivingAddress().getAddress()
        );
    }
}
