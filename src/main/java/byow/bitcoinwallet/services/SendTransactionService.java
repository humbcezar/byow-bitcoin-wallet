package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.IntStream.range;

@Component
@Lazy
public class SendTransactionService {
    private FeeEstimator feeEstimator;

    private TransactionCreator transactionCreator;

    private TransactionSender transactionSender;

    private CurrentAddressesManager currentAddressesManager;

    private CurrentWalletManager currentWalletManager;

    private NextChangeAddress nextChangeAddress;

    @Autowired
    public SendTransactionService(
        FeeEstimator feeEstimator,
        TransactionCreator transactionCreator,
        TransactionSender transactionSender,
        CurrentAddressesManager currentAddressesManager,
        CurrentWalletManager currentWalletManager,
        NextChangeAddress nextChangeAddress
    ) {
        this.feeEstimator = feeEstimator;
        this.transactionCreator = transactionCreator;
        this.transactionSender = transactionSender;
        this.currentAddressesManager = currentAddressesManager;
        this.currentWalletManager = currentWalletManager;
        this.nextChangeAddress = nextChangeAddress;
    }

    public void send(String addressToSend, BigDecimal amountToSend) {
        List<Unspent> utxos = currentAddressesManager.getUtxos();
        BigDecimal feeRate = feeEstimator.estimate();
        Transaction transaction = transactionCreator.create(
            utxos,
            amountToSend,
            feeRate,
            currentAddressesManager.getReceivingAddressesMap(),
            currentWalletManager.getCurrentWallet().getSeed(),
            addressToSend,
            nextChangeAddress.getReceivingAddress().getAddress()
        );
        range(0, transaction.getInputCount()).forEach(transaction::sign);
        transactionSender.send(transaction);
    }
}
