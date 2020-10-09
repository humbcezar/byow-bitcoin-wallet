package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

@ActiveProfiles("test")
@SpringBootTest
public class DefaultAddressGeneratorTest {
    @Autowired
    private DefaultAddressGenerator addressGenerator;

    @Autowired
    private WalletCreator walletCreator;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        initMocks(this);
        walletCreator.setApplicationEventPublisher(this.applicationEventPublisher);
    }

    @ParameterizedTest
    @MethodSource("testGenerateSegwitAddressArguments")
    public void testGenerateSegwitAddress(
            String walletName,
            String mnemonicSeed,
            String password,
            String expectedAddress,
            DerivationPath derivationPath
    ) {
        Wallet wallet = walletCreator.create(
                walletName,
                mnemonicSeed,
                password
        );
        String address = addressGenerator.generate(wallet, derivationPath);
        assertEquals(expectedAddress, address);
    }

    private static Stream<Arguments> testGenerateSegwitAddressArguments() {
        return Stream.of(
                Arguments.of(
                        "test",
                        "gap print mobile track security horn polar female inhale liberty general benefit",
                        "",
                        "bc1qzvfaa0r54fnlfsdv745h2npe7uuwph0892323l",
                        FIRST_BIP84_ADDRESS_PATH
                )
        );
    }
}
