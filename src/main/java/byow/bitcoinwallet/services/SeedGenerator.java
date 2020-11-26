package byow.bitcoinwallet.services;

import com.blockstream.libwally.Wally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class SeedGenerator {

    private Object wordList;

    private EntropyCreator entropyCreator;

    public SeedGenerator() {
    }

    @Autowired
    public SeedGenerator(@Qualifier("wordList") Object wordList, EntropyCreator entropyCreator) {
        this.wordList = wordList;
        this.entropyCreator = entropyCreator;
    }

    public String generateMnemonicSeed() {
        String mnemonicSeed = Wally.bip39_mnemonic_from_bytes(wordList, entropyCreator.createEntropy());
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mnemonicSeed;
    }

    public String generateSeed(String mnemonicSeed, String password) {
        final byte[] seed = new byte[Wally.BIP39_SEED_LEN_512];
        Wally.bip39_mnemonic_to_seed(mnemonicSeed, password, seed);
        return Wally.hex_from_bytes(seed);
    }
}
