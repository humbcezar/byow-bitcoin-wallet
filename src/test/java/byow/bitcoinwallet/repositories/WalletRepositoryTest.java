package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.Wallet;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WalletRepositoryTest {
    @Autowired
    public WalletRepository walletRepository;

    @Test
    public void save() {
        String name = RandomString.make();
        String seed = RandomString.make();
        Wallet wallet = new Wallet(name, seed);
        wallet.setCreatedAt(new Date());
        walletRepository.save(wallet);
        Wallet wallet2 = walletRepository.findByName(name);
        assertEquals(wallet.getSeed(), wallet2.getSeed());
        assertEquals(wallet.getName(), wallet2.getName());
        assertNotNull(wallet2.getCreatedAt());
    }

    @Test
    public void saveRepeatedNameFails() {
        String name = RandomString.make();
        String seed = RandomString.make();
        String seed2 = RandomString.make();

        Wallet wallet = new Wallet(name, seed);
        walletRepository.save(wallet);
        Wallet wallet2 = new Wallet(name, seed2);
        assertThrows(DataIntegrityViolationException.class, () -> walletRepository.save(wallet2));
    }

    @Test
    public void saveRepeatedSeedFails() {
        String name = RandomString.make();
        String name2 = RandomString.make();
        String seed = RandomString.make();

        Wallet wallet = new Wallet(name, seed);
        walletRepository.save(wallet);
        Wallet wallet2 = new Wallet(name2, seed);
        assertThrows(DataIntegrityViolationException.class, () -> walletRepository.save(wallet2));
    }
}
