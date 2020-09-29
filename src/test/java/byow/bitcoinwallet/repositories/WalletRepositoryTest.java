package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.Wallet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
public class WalletRepositoryTest {
    @Autowired
    public WalletRepository walletRepository;

    @Test
    public void save() {
        Wallet wallet = new Wallet("name", "seed");
        walletRepository.save(wallet);
        Wallet wallet2 = walletRepository.findByName("name");
        Assertions.assertEquals(wallet.getMnemonicSeed(), wallet2.getMnemonicSeed());
        Assertions.assertEquals(wallet.getName(), wallet2.getName());
    }

    @Test
    public void saveRepeatedNameFails() {
        Wallet wallet = new Wallet("name", "DDE");
        walletRepository.save(wallet);
        Wallet wallet2 = new Wallet("name", "ABC");
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> walletRepository.save(wallet2));
    }

    @Test
    public void saveRepeatedSeedFails() {
        Wallet wallet = new Wallet("abc", "ABC");
        walletRepository.save(wallet);
        Wallet wallet2 = new Wallet("name", "ABC");
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> walletRepository.save(wallet2));
    }
}
