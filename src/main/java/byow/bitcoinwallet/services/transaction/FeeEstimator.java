package byow.bitcoinwallet.services.transaction;

import java.math.BigDecimal;

public interface FeeEstimator {
    BigDecimal estimate();
}
