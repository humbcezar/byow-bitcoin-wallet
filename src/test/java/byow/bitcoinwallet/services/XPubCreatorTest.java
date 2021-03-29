package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.entities.XPub;
import byow.bitcoinwallet.entities.XPubTypes;
import byow.bitcoinwallet.repositories.XPubRepository;
import byow.bitcoinwallet.services.address.*;
import byow.bitcoinwallet.services.wallet.XPubCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class XPubCreatorTest {
    private final SeedGenerator seedGenerator = new SeedGenerator();

    private XPubCreator xPubCreator;

    private XPubRepository xPubRepository;

    private List<XPubKeyGenerator> xPubKeyGenerators;

    private DefaultXPubKeyGenerator defaultXPubKeyGenerator;

    private ChangeXPubKeyGenerator changeXPubKeyGenerator;

    private NestedSegwitXPubKeyGenerator nestedSegwitXPubKeyGenerator;

    private ChangeNestedSegwitXPubKeyGenerator changeNestedSegwitXPubKeyGenerator;

    @BeforeEach
    public void setup() {
        xPubRepository = mock(XPubRepository.class);
        defaultXPubKeyGenerator = mock(DefaultXPubKeyGenerator.class);
        changeXPubKeyGenerator = mock(ChangeXPubKeyGenerator.class);
        nestedSegwitXPubKeyGenerator = mock(NestedSegwitXPubKeyGenerator.class);
        changeNestedSegwitXPubKeyGenerator = mock(ChangeNestedSegwitXPubKeyGenerator.class);
        xPubKeyGenerators = List.of(
            defaultXPubKeyGenerator,
            changeXPubKeyGenerator,
            nestedSegwitXPubKeyGenerator,
            changeNestedSegwitXPubKeyGenerator
        );
        xPubCreator = new XPubCreator(xPubRepository, xPubKeyGenerators);
    }

    @Test
    public void createXPubs() {
        String mnemonicSeed = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        long walletId = 1;
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        ArgumentCaptor<XPub> xPubCaptor = ArgumentCaptor.forClass(XPub.class);

        when(defaultXPubKeyGenerator.getType()).thenReturn(XPubTypes.DEFAULT_X_PUB);
        when(changeXPubKeyGenerator.getType()).thenReturn(XPubTypes.CHANGE_X_PUB);
        when(nestedSegwitXPubKeyGenerator.getType()).thenReturn(XPubTypes.NESTED_SEGWIT_X_PUB);
        when(changeNestedSegwitXPubKeyGenerator.getType()).thenReturn(XPubTypes.CHANGE_NESTED_SEGWIT_X_PUB);

        xPubCreator.create(seed, wallet);

        verify(defaultXPubKeyGenerator).generateXPubkeySerialized(seed);
        verify(changeXPubKeyGenerator).generateXPubkeySerialized(seed);
        verify(nestedSegwitXPubKeyGenerator).generateXPubkeySerialized(seed);
        verify(changeNestedSegwitXPubKeyGenerator).generateXPubkeySerialized(seed);
        verify(xPubRepository, times(4)).save(xPubCaptor.capture());
        xPubCaptor.getAllValues().forEach(xPub -> assertEquals(xPub.getWallet().getId(), walletId));
    }

}