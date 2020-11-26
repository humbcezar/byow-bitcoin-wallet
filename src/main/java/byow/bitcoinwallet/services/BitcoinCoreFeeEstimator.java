package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;

import static java.util.Objects.isNull;

@Component
@Lazy
public class BitcoinCoreFeeEstimator implements FeeEstimator {
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Value("${bitcoin.fallbackFee}")
    private BigDecimal fallbackFee;

    @Override
    public BigDecimal estimate() {
        BigDecimal feeRate = bitcoindRpcClient.estimateSmartFee(1).feeRate();
        if (isNull(feeRate)) {
            return fallbackFee;
        }
        return feeRate;
    }
}
