package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.wallet.WalletCreator;
import byow.bitcoinwallet.services.wallet.XPubCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ActiveProfiles("test")
@SpringBootTest
public class WalletCreatorTest {
    @Autowired
    WalletCreator walletCreator;
    @Autowired
    Encryptor encryptor;
    @MockBean
    WalletRepository walletRepository;
    @MockBean
    ApplicationEventPublisher applicationEventPublisher;
    @MockBean
    XPubCreator xPubCreator;

    @BeforeEach
    void setUp() {
        initMocks(this);
        walletCreator.setApplicationEventPublisher(this.applicationEventPublisher);
    }

    @Test
    public void create() {
        String mnemonicSeed = "crazy mosquito liberty anger sort pudding toward tenant credit demise field borrow";
        String expectedSeed = "e2aaf320defa79d6b62383060b1d123179d2de507834cace3d8ce6550aa587344c438e544d69d42aec851a94458e92dd347feeed8b44eea628345015cdece780";
        when(walletRepository.findById(any())).thenReturn(Optional.of(new Wallet()));
        Wallet wallet = walletCreator.create("Test name", mnemonicSeed, "");
        assertEquals("Test name", wallet.getName());
        assertEquals(expectedSeed, encryptor.decrypt(wallet.getSeed(), ""));
        verify(xPubCreator).create(expectedSeed, wallet);
        verify(walletRepository, times(1)).save(wallet);
        verify(applicationEventPublisher, times(1)).publishEvent(any());
    }

    @Test
    public void createWithPassword() {
        String mnemonicSeed = "crazy mosquito liberty anger sort pudding toward tenant credit demise field borrow";
        String expectedSeed = "052eb7ad096242bc46a24f1923a216bdd94855dd9bc4e116bc94b2ccf23ef54c9cc7e65ef70af2d2900f56277d1179d7739696ec694c8fb217c08eff849e8123";
        when(walletRepository.findById(any())).thenReturn(Optional.of(new Wallet()));
        Wallet wallet = walletCreator.create(
            "Test name",
            mnemonicSeed,
            "password"
        );
        assertEquals("Test name", wallet.getName());
        assertEquals(expectedSeed, encryptor.decrypt(wallet.getSeed(), "password"));
        verify(xPubCreator).create(expectedSeed, wallet);
        verify(walletRepository, times(1)).save(wallet);
        verify(applicationEventPublisher, times(1)).publishEvent(any());
    }
}
