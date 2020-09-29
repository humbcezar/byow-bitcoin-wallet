package byow.bitcoinwallet.services;

import com.blockstream.libwally.Wally;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class EntropyCreator {
    public byte[] createEntropy() {
        byte[] entropy = new byte[Wally.BIP32_ENTROPY_LEN_128];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(entropy);
        return entropy;
    }
}
