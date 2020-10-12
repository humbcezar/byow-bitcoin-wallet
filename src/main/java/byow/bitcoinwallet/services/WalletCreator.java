package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.blockstream.libwally.Wally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class WalletCreator {
    private WalletRepository walletRepository;
    private Object wordList;
    private EntropyCreator entropyCreator;
    private ApplicationEventPublisher applicationEventPublisher;
    private SeedGenerator seedGenerator;

    @Autowired
    public WalletCreator(
            WalletRepository walletRepository,
            @Qualifier("wordList") Object wordList,
            EntropyCreator entropyCreator,
            ApplicationEventPublisher applicationEventPublisher,
            SeedGenerator seedGenerator
    ) {
        this.walletRepository = walletRepository;
        this.wordList = wordList;
        this.entropyCreator = entropyCreator;
        this.applicationEventPublisher = applicationEventPublisher;
        this.seedGenerator = seedGenerator;
    }

    public Wallet create(String walletName, String mnemonicSeed, String password) {
        Wallet wallet = new Wallet(walletName, seedGenerator.generateSeed(mnemonicSeed, password));
        walletRepository.save(wallet);
        this.applicationEventPublisher.publishEvent(new WalletCreatedEvent(this, wallet));
        return wallet;
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

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
