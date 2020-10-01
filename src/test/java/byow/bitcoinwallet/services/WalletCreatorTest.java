package byow.bitcoinwallet.services;

import byow.bitcoinwallet.TestBase;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.blockstream.libwally.Wally;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class WalletCreatorTest extends TestBase {
    @Autowired
    WalletCreator walletCreator;
    @MockBean
    WalletRepository walletRepository;

    @Test
    public void generateMnemonicSeed() {
        String langEn = Wally.bip39_get_languages().split(" ")[0];
        Object wordList = Wally.bip39_get_wordlist(langEn);
        String mnemonicSeed = walletCreator.generateMnemonicSeed();
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
        } catch (final Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void create() {
        String mnemonicSeed = "crazy mosquito liberty anger sort pudding toward tenant credit demise field borrow";
        String expectedSeed = "e2aaf320defa79d6b62383060b1d123179d2de507834cace3d8ce6550aa587344c438e544d69d42aec851a94458e92dd347feeed8b44eea628345015cdece780";
        Wallet wallet = walletCreator.create("Test name", mnemonicSeed, "");
        assertEquals("Test name", wallet.getName());
        assertEquals(expectedSeed, wallet.getSeed());
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    public void createWithPassword() {
        String mnemonicSeed = "crazy mosquito liberty anger sort pudding toward tenant credit demise field borrow";
        String expectedSeed = "052eb7ad096242bc46a24f1923a216bdd94855dd9bc4e116bc94b2ccf23ef54c9cc7e65ef70af2d2900f56277d1179d7739696ec694c8fb217c08eff849e8123";
        Wallet wallet = walletCreator.create(
            "Test name",
            mnemonicSeed,
            "password"
        );
        assertEquals("Test name", wallet.getName());
        assertEquals(expectedSeed, wallet.getSeed());
        verify(walletRepository, times(1)).save(wallet);
    }
}
