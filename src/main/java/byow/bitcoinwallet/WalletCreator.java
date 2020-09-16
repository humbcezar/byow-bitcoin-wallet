package byow.bitcoinwallet;

import com.blockstream.libwally.Wally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class WalletCreator {
    @Autowired
    WalletRepository walletRepository;

    public Wallet create(String name, String mnemonicSeed) {
        Wallet wallet = new Wallet();
        wallet.setMnemonicSeed(mnemonicSeed);
        wallet.setName(name);
        walletRepository.save(wallet);
        return wallet;
    }

    public String generateMnemonicSeed() {
        String langEn = Wally.bip39_get_languages().split(" ")[0];
        Object wordList = Wally.bip39_get_wordlist(langEn);
        byte[] entropy = new byte[Wally.BIP32_ENTROPY_LEN_128];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(entropy);
        String mnemonicSeed = Wally.bip39_mnemonic_from_bytes(wordList, entropy);
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mnemonicSeed;
    }
}
