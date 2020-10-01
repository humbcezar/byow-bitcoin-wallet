package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.blockstream.libwally.Wally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class WalletCreator {
    private WalletRepository walletRepository;
    private Object wordList;
    private EntropyCreator entropyCreator;

    @Autowired
    public WalletCreator(
            WalletRepository walletRepository,
            @Qualifier("wordList") Object wordList,
            EntropyCreator entropyCreator
    ) {
        this.walletRepository = walletRepository;
        this.wordList = wordList;
        this.entropyCreator = entropyCreator;
    }

    public Wallet create(String walletName, String mnemonicSeed, String password) {
        Wallet wallet = new Wallet(walletName, createSeed(mnemonicSeed, password));
        walletRepository.save(wallet);
        return wallet;
    }

    private String createSeed(String mnemonicSeed, String password) {
        final byte[] seed = new byte[Wally.BIP39_SEED_LEN_512];
        Wally.bip39_mnemonic_to_seed(mnemonicSeed, password, seed);
        return Wally.hex_from_bytes(seed);
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
}
