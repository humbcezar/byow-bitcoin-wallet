package byow.bitcoinwallet.services.address;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import static com.blockstream.libwally.Wally.BIP32_ENTROPY_LEN_256;

@Component
public class EntropyCreator {
    public byte[] createEntropy() {
        byte[] entropy = new byte[BIP32_ENTROPY_LEN_256];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(entropy);
        return entropy;
    }
}
