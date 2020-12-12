package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;

@Component
public class DustCalculator {
    private long dustRelayFee;

    public boolean isDust(long amountInSatoshis) {
        return amountInSatoshis < 98 * dustRelayFee / 1000;
    }

    public boolean isDust(BigDecimal amountInBtc) {
        return isDust(valueOf(100_000_000).multiply(amountInBtc).longValue());
    }

    @Value("${bitcoin.network.dustRelayFee}")
    public void setDustRelayFee(long dustRelayFee) {
        this.dustRelayFee = dustRelayFee;
    }
}
