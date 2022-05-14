package byow.bitcoinwallet.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static java.math.BigDecimal.valueOf;

public class SatoshiUtils {
    public static long btcToSatoshi(BigDecimal amount) {
        return amount.multiply(valueOf(100000000)).longValueExact();
    }
    public static BigDecimal satoshiToBtc(BigInteger amount) {
        return new BigDecimal(amount).divide(BigDecimal.valueOf(100_000_000), 8, RoundingMode.UNNECESSARY);
    }
}
