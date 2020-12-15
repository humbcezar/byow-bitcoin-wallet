package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.blockstream.libwally.Wally.WALLY_TX_DUMMY_SIG_LOW_R;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.FLOOR;
import static java.util.Collections.shuffle;
import static java.util.Objects.isNull;

@Component
public class SingleRandomDrawCoinSelector implements CoinSelector {
    public static final long N_SEQUENCE = 4294967295L;

    private static final long INITIAL_DEFAULT_CHANGE_VALUE = 1;

    private DefaultKeyGenerator defaultKeyGenerator;

    private String addressPrefix;

    private DustCalculator dustCalculator;

    @Autowired
    public SingleRandomDrawCoinSelector(
        DefaultKeyGenerator defaultKeyGenerator,
        @Qualifier("addressPrefix") String addressPrefix,
        DustCalculator dustCalculator
    ) {
        this.defaultKeyGenerator = defaultKeyGenerator;
        this.addressPrefix = addressPrefix;
        this.dustCalculator = dustCalculator;
    }

    @Override
    public Transaction select(
        List<Unspent> utxos,
        BigDecimal target,
        BigDecimal feeRate,
        Map<String, ReceivingAddress> receivingAddressMap,
        String seed,
        String toAddress,
        String changeAddress
    ) {
        utxos = preProcess(utxos);

        long targetInSatoshis = satoshis(target);
        long feeRateInSatoshisPerByte = btcPerKbToSatoshiPerByte(feeRate);

        List<TransactionInput> transactionInputs = new ArrayList<>();
        List<Unspent> shuffledCoins = new ArrayList<>(utxos);
        shuffle(shuffledCoins);
        Transaction transaction = null;

        for (Unspent utxo : shuffledCoins) {
            DerivationPath derivationPath = receivingAddressMap.get(utxo.address()).getDerivationPath();
            transactionInputs.add(createInput(utxo, derivationPath, seed));
            long totalInputBalance = totalInputBalance(transactionInputs);
            if (totalInputBalance < targetInSatoshis) {
                continue;
            }

            List<TransactionOutput> transactionOutputs = createOutputs(totalInputBalance, targetInSatoshis, toAddress, changeAddress);
            transaction = new Transaction(transactionInputs.size(), transactionOutputs.size());
            transactionInputs.forEach(transaction::addInput);
            transactionOutputs.forEach(transaction::addOutput);
            long totalFeeInSatoshis = transaction.vSize() * feeRateInSatoshisPerByte;
            long adjustedTarget = adjustedTarget(targetInSatoshis, totalFeeInSatoshis);
            if (totalInputBalance > adjustedTarget) {
                transaction.removeOutput(1);
                TransactionOutput changeOutput = new TransactionOutput(
                    changeAddress,
                    addressPrefix,
                    totalInputBalance - adjustedTarget
                );
                transaction.addOutput(changeOutput);
                transaction.setFeeRateInSatoshisPerByte(feeRateInSatoshisPerByte);
                transaction.setTotalFeeInSatoshis(totalFeeInSatoshis);
                transaction.setIntendedTotalFeeInSatoshis(totalFeeInSatoshis);
                break;
            }
        }

        if (changeIsDust(transaction)) {
            transaction.removeOutput(1);
            long intendedTotalFeeInSatoshis = transaction.vSize() * feeRateInSatoshisPerByte;
            transaction.setIntendedTotalFeeInSatoshis(intendedTotalFeeInSatoshis);
            long totalInputBalance = totalInputBalance(transactionInputs);
            long totalFeeInSatoshis = totalInputBalance - targetInSatoshis;
            transaction.setTotalFeeInSatoshis(totalFeeInSatoshis);
        }

        if (!isNull(transaction) && transaction.getOutputCount() == 1) {
            long intendedTotalFeeInSatoshis = transaction.vSize() * feeRateInSatoshisPerByte;
            transaction.setIntendedTotalFeeInSatoshis(intendedTotalFeeInSatoshis);
            long totalInputBalance = totalInputBalance(transactionInputs);
            long totalFeeInSatoshis = totalInputBalance - targetInSatoshis;
            transaction.setTotalFeeInSatoshis(totalFeeInSatoshis);
        }

        return transaction;
    }

    private List<Unspent> preProcess(List<Unspent> utxos) {
        return utxos.stream()
            .filter(utxo -> utxo.confirmations() > 0)
            .collect(Collectors.toList());
    }

    private boolean changeIsDust(Transaction transaction) {
        return !isNull(transaction) && transaction.getOutputCount() > 1 && dustCalculator.isDust(transaction.getOutput(1).getAmount());
    }

    private long btcPerKbToSatoshiPerByte(BigDecimal feeRate) {
        return feeRate.multiply(valueOf(100000000))
            .divide(valueOf(1024), new MathContext(2, FLOOR))
            .longValue();
    }

    private List<TransactionOutput> createOutputs(long totalInputBalance, long targetInSatoshis, String toAddress, String changeAddress) {
        TransactionOutput toAddressOutput = new TransactionOutput(toAddress, addressPrefix, targetInSatoshis);
        if (totalInputBalance == targetInSatoshis) {
            return List.of(toAddressOutput);
        }
        TransactionOutput changeOutput = new TransactionOutput(changeAddress, addressPrefix, INITIAL_DEFAULT_CHANGE_VALUE);
        return List.of(toAddressOutput, changeOutput);
    }

    private TransactionInput createInput(Unspent utxo, DerivationPath derivationPath, String seed) { //TODO: refactor
        byte[] publicKey = defaultKeyGenerator.getPublicKeyAsByteArray(seed, derivationPath);
        byte[] privateKey = defaultKeyGenerator.getPrivateKeyAsByteArray(seed, derivationPath);

        Witness witness = new Witness(2);
        witness.addDummySignature(WALLY_TX_DUMMY_SIG_LOW_R);
        witness.addPublicKey(publicKey);

        return new TransactionInput(
            utxo.txid(),
            utxo.vout(),
            satoshis(utxo.amount()),
            privateKey,
            N_SEQUENCE,
            null,
            witness
        );
    }

    private long totalInputBalance(List<TransactionInput> transactionInputs) {
        return transactionInputs.stream()
            .map(TransactionInput::getAmountInSatoshis)
            .reduce(Long::sum)
            .orElse(0L);
    }

    private long adjustedTarget(long targetInSatoshis, long totalFeeInSatoshis) {
        return targetInSatoshis + totalFeeInSatoshis;
    }

    private long satoshis(BigDecimal amount) {
        return amount.multiply(valueOf(100000000)).longValueExact();
    }
}
