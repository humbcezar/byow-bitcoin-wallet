package byow.bitcoinwallet.services.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultXPubKeyGeneratorTest {
    private SeedGenerator seedGenerator;

    private DefaultXPubKeyGenerator defaultXPubKeyGenerator;

    @BeforeEach
    public void setup() {
        seedGenerator = new SeedGenerator();
        defaultXPubKeyGenerator = new DefaultXPubKeyGenerator(new DefaultKeyGenerator());
    }

    @ParameterizedTest
    @MethodSource("testCreateDefaultXPubTestCases")
    public void testCreateDefaultXPub(String mnemonicSeed, String expectedXPub) {
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        assertEquals(expectedXPub, defaultXPubKeyGenerator.generateXPubkeySerialized(seed));
    }

    private static Stream<Arguments> testCreateDefaultXPubTestCases() {
        return Stream.of(
            Arguments.of(
                "trade medal sense lawn east quick web income broccoli truly dizzy bird address pencil spot",
                "zpub6tiAJGSWNFP9fAtvudZBGNLwPx6zuYqs33C14rqgAp5rkTkT9BbNiAewhKM8rMJf3BzpGtqbgN6wWrrBs1UazbHuWm49kucxmAKK1pTZ86b"
            ),
            Arguments.of(
                "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
                "zpub6u4KbU8TSgNuZSxzv7HaGq5Tk361gMHdZxnM4UYuwzg5CMLcNytzhobitV4Zq6vWtWHpG9QijsigkxAzXvQWyLRfLq1L7VxPP1tky1hPfD4"
            )
        );
    }
}