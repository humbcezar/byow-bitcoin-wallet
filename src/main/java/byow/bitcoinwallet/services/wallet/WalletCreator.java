package byow.bitcoinwallet.services.wallet;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.events.WalletCreatedEvent;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.AuthenticationService;
import byow.bitcoinwallet.services.Encryptor;
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

    private final Encryptor encryptor;

    private final XPubCreator xPubCreator;

    @Autowired
    public WalletCreator(
        WalletRepository walletRepository,
        @Qualifier("wordList") Object wordList,
        EntropyCreator entropyCreator,
        ApplicationEventPublisher applicationEventPublisher,
        SeedGenerator seedGenerator,
        AuthenticationService authenticationService,
        Encryptor encryptor,
        XPubCreator xPubCreator
    ) {
        this.walletRepository = walletRepository;
        this.wordList = wordList;
        this.entropyCreator = entropyCreator;
        this.applicationEventPublisher = applicationEventPublisher;
        this.seedGenerator = seedGenerator;
        this.authenticationService = authenticationService;
        this.encryptor = encryptor;
        this.xPubCreator = xPubCreator;
    }

    public Wallet create(String walletName, String mnemonicSeed, String password) {
        return create(walletName, mnemonicSeed, password, new Date());
    }

    public Wallet create(String walletName, String mnemonicSeed, String password, Date walletCreationDate) {
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, password);
        Wallet wallet = new Wallet(
            walletName,
            encryptor.encrypt(seed, password),
            authenticationService.hashPassword(password)
        );
        wallet.setCreatedAt(walletCreationDate);
        walletRepository.save(wallet);
        xPubCreator.create(seed, wallet);
        this.applicationEventPublisher.publishEvent(new WalletCreatedEvent(this, fresh(wallet)));
        return wallet;
    }

    private Wallet fresh(Wallet wallet) {
        return walletRepository.findById(wallet.getId())
            .orElseThrow();
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
