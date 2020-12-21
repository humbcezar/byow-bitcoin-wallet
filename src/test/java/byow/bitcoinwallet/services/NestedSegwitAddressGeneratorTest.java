package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
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

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP49_ADDRESS_PATH;
import static com.blockstream.libwally.Wally.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ActiveProfiles("test")
@SpringBootTest
public class NestedSegwitAddressGeneratorTest {

    private NestedSegwitAddressGenerator addressGenerator;

    @Autowired
    private DefaultKeyGenerator defaultKeyGenerator;

    @Autowired
    private WalletCreator walletCreator;

    @Autowired
    private WalletRepository walletRepository;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        initMocks(this);
        walletRepository.deleteAll();
        walletCreator.setApplicationEventPublisher(this.applicationEventPublisher);
        addressGenerator = new NestedSegwitAddressGenerator(defaultKeyGenerator, WALLY_ADDRESS_VERSION_P2SH_MAINNET);
    }

    @ParameterizedTest
    @MethodSource("testGenerateNestedSegwitAddressArguments")
    void testGenerateNestedSegwitAddress(
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
        String address = addressGenerator.generate(wallet.getSeed(), derivationPath);
        assertEquals(expectedAddress, address);
    }

    private static Stream<Arguments> testGenerateNestedSegwitAddressArguments() {
        return Stream.of(
            Arguments.of(
                "bip49-test-vector-1",
                "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
                "",
                "37VucYSaXLCAsxYyAPfbSi9eh4iEcbShgf",
                FIRST_BIP49_ADDRESS_PATH
            ),
            Arguments.of(
                "test2",
                "giggle green cricket salon race retire domain ancient bone spoon green cycle direct memory hand",
                "",
                "36GPJ7Tb9yL1rrToUAzygr536S2uKxJuNg",
                FIRST_BIP49_ADDRESS_PATH
            ),
            Arguments.of(
                "test3",
                "giggle green cricket salon race retire domain ancient bone spoon green cycle direct memory hand",
                "",
                "345AYwXux2ZGA2zdq6BBsWAPzAHE8NYWZF",
                new DerivationPath("49'/0'/0'/0/3")
            ),
            Arguments.of(
                "test4",
                "giggle green cricket salon race retire domain ancient bone spoon green cycle direct memory hand",
                "",
                "3E2B7P23s48zocTGbhwco5HiakeHj2koDK",
                new DerivationPath("49'/0'/0'/1/3")
            ),
            Arguments.of(
                "test5",
                "giggle green cricket salon race retire domain ancient bone spoon green cycle direct memory hand",
                "testpassword",
                "36DCqGQVx6HM6sq1WSrdX8Csz8mJZK6qsg",
                new DerivationPath("49'/0'/0'/0/5")
            )
        );
    }
}