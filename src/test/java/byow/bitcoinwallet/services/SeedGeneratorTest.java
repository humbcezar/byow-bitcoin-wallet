package byow.bitcoinwallet.services;

import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.services.address.SeedGenerator;
import com.blockstream.libwally.Wally;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class SeedGeneratorTest {
    @Autowired
    private SeedGenerator seedGenerator;

    @Test
    public void generateMnemonicSeed() {
        Object wordList = Wally.bip39_get_wordlist(Languages.EN);
        String mnemonicSeed = seedGenerator.generateMnemonicSeed();
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
        } catch (final Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void generateSeed() {
        String mnemonicSeed = seedGenerator.generateMnemonicSeed();
        String password = "";
        String seed = seedGenerator.generateSeed(mnemonicSeed, password);
        assertFalse(seed.isEmpty());
    }
}
