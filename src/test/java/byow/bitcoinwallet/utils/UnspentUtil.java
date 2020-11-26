package byow.bitcoinwallet.utils;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;

@Component
@Profile("test")
public class UnspentUtil {

    public Unspent unspent(String expectedAddress, BigDecimal amount, int confirmations) {
        return new Unspent() {
            @Override
            public String account() {
                return "test";
            }

            @Override
            public int confirmations() {
                return confirmations;
            }

            @Override
            public String txid() {
                return "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6";
            }

            @Override
            public Integer vout() {
                return 0;
            }

            @Override
            public String scriptPubKey() {
                return null;
            }

            @Override
            public String address() {
                return expectedAddress;
            }

            @Override
            public BigDecimal amount() {
                return amount;
            }

            @Override
            public byte[] data() {
                return new byte[0];
            }
        };
    }
}
