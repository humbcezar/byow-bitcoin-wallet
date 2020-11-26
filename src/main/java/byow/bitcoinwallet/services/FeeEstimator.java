package byow.bitcoinwallet.services;

import java.math.BigDecimal;

public interface FeeEstimator {
    BigDecimal estimate();
}
