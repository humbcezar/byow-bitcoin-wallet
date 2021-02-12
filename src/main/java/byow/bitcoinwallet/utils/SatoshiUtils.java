package byow.bitcoinwallet.utils;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;

public class SatoshiUtils {
    public static long btcToSatoshi(BigDecimal amount) {
        return amount.multiply(valueOf(100000000)).longValueExact();
    }
}
