package byow.bitcoinwallet.services.wallet;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.AuthenticationService;
import byow.bitcoinwallet.services.address.EntropyCreator;
import byow.bitcoinwallet.services.address.SeedGenerator;
import com.blockstream.libwally.Wally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WalletCreator {
    private final WalletRepository walletRepository;

    private final Object wordList;

    private final EntropyCreator entropyCreator;

    private ApplicationEventPublisher applicationEventPublisher;

    private final SeedGenerator seedGenerator;

    private final AuthenticationService authenticationService;

    @Autowired
    public WalletCreator(
        WalletRepository walletRepository,
        @Qualifier("wordList") Object wordList,
        EntropyCreator entropyCreator,
        ApplicationEventPublisher applicationEventPublisher,
        SeedGenerator seedGenerator,
        AuthenticationService authenticationService
    ) {
        this.walletRepository = walletRepository;
        this.wordList = wordList;
        this.entropyCreator = entropyCreator;
        this.applicationEventPublisher = applicationEventPublisher;
        this.seedGenerator = seedGenerator;
        this.authenticationService = authenticationService;
    }

    public Wallet create(String walletName, String mnemonicSeed, String password) {
        return create(walletName, mnemonicSeed, password, new Date());
    }

    public Wallet create(String walletName, String mnemonicSeed, String password, Date walletCreationDate) {
        Wallet wallet = new Wallet(walletName, seedGenerator.generateSeed(mnemonicSeed, password), authenticationService.hashPassword(password));
        wallet.setCreatedAt(walletCreationDate);
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
