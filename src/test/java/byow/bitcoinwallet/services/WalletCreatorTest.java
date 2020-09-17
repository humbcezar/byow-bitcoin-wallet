package byow.bitcoinwallet.services;

import byow.bitcoinwallet.TestBase;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.WalletCreator;
import com.blockstream.libwally.Wally;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;


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
        Wallet wallet = walletCreator.create("Test name", "mnemonic seed");
        Assertions.assertEquals("Test name", wallet.getName());
        Assertions.assertEquals("mnemonic seed", wallet.getMnemonicSeed());
        Mockito.verify(walletRepository, Mockito.times(1)).save(wallet);
    }
}
