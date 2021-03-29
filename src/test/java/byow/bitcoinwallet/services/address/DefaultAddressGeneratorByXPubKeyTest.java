package byow.bitcoinwallet.services.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP84_CHANGE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultAddressGeneratorByXPubKeyTest {
    private DefaultAddressGeneratorByXPubKey defaultAddressGeneratorByXPubKey;

    @BeforeEach
    public void setup() {
        defaultAddressGeneratorByXPubKey = new DefaultAddressGeneratorByXPubKey(new DefaultKeyGenerator(), "bc");
    }

    @ParameterizedTest
    @MethodSource("createTestCases")
    public void generate(String xPubKey, DerivationPath derivationPath, String expectedAddress) {
        String address = defaultAddressGeneratorByXPubKey.generate(xPubKey, derivationPath);
        assertEquals(expectedAddress, address);
    }

    private static Stream<Arguments> createTestCases() {
        return Stream.of(
            Arguments.of(
                "zpub6tdg7e5Qro2NsZRo4BKoRBe6mucBsVHrgqHLMRBxhjQaP3cgDdrzF9zV7KfZWK9P95Zwf7TuJQRdqcEwzbHsEbSmLUZNUK5MYCEWQ7NF3wU",
                FIRST_BIP84_ADDRESS_PATH,
                "bc1q23an6z9d0r7ezyhecrge4cxn5t7ruue2aypvj5"
            ),
            Arguments.of(
                "zpub6tdg7e5Qro2NsZRo4BKoRBe6mucBsVHrgqHLMRBxhjQaP3cgDdrzF9zV7KfZWK9P95Zwf7TuJQRdqcEwzbHsEbSmLUZNUK5MYCEWQ7NF3wU",
                FIRST_BIP84_ADDRESS_PATH.next(1),
                "bc1qvrayqyw09th06jhn9zsrda4g8vfcspzxx8w2et"
            ),
            Arguments.of(
                "zpub6u4KbU8TSgNuZSxzv7HaGq5Tk361gMHdZxnM4UYuwzg5CMLcNytzhobitV4Zq6vWtWHpG9QijsigkxAzXvQWyLRfLq1L7VxPP1tky1hPfD4",
                FIRST_BIP84_ADDRESS_PATH,
                "bc1qcr8te4kr609gcawutmrza0j4xv80jy8z306fyu"
            ),
            Arguments.of(
                "zpub6u4KbU8TSgNuZSxzv7HaGq5Tk361gMHdZxnM4UYuwzg5CMLcNytzhobitV4Zq6vWtWHpG9QijsigkxAzXvQWyLRfLq1L7VxPP1tky1hPfD4",
                FIRST_BIP84_ADDRESS_PATH.next(1),
                "bc1qnjg0jd8228aq7egyzacy8cys3knf9xvrerkf9g"
            ),
            Arguments.of(
                "zpub6u4KbU8TSgNuco8HzL1LqM2ePjv8wrxUKENTtfambyxBbACZg5qvqqzAPwwAopTuxkrQzs661k5A6Q1P8b25a9DDJXYXDpN4KPwxygrx9Py",
                FIRST_BIP84_CHANGE_PATH,
                "bc1q8c6fshw2dlwun7ekn9qwf37cu2rn755upcp6el"
            ),
            Arguments.of(
                "zpub6u4KbU8TSgNuco8HzL1LqM2ePjv8wrxUKENTtfambyxBbACZg5qvqqzAPwwAopTuxkrQzs661k5A6Q1P8b25a9DDJXYXDpN4KPwxygrx9Py",
                FIRST_BIP84_CHANGE_PATH.next(1),
                "bc1qggnasd834t54yulsep6fta8lpjekv4zj6gv5rf"
            )
        );
    }
}