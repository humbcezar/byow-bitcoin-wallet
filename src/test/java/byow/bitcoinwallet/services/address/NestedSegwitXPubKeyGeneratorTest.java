package byow.bitcoinwallet.services.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedSegwitXPubKeyGeneratorTest {
    private SeedGenerator seedGenerator;

    private NestedSegwitXPubKeyGenerator nestedSegwitXPubKeyGenerator;

    @BeforeEach
    public void setup() {
        seedGenerator = new SeedGenerator();
        nestedSegwitXPubKeyGenerator = new NestedSegwitXPubKeyGenerator(new DefaultKeyGenerator());
    }

    @ParameterizedTest
    @MethodSource("testCreateNestedSegwitXPubTestCases")
    public void testCreateNestedSegwitXPub(String mnemonicSeed, String expectedXPub) {
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        assertEquals(expectedXPub, nestedSegwitXPubKeyGenerator.generateXPubkeySerialized(seed));
    }

    private static Stream<Arguments> testCreateNestedSegwitXPubTestCases() {
        return Stream.of(
            Arguments.of(
                "hero parent remind faint wife ride source boat squeeze seed lock coast wrong emotion detect",
                "ypub6ZtDiwau7w6qeAbfWuesfgY1UNbWi2xUJWw8bcDg63rGxdWKUiHiKzRbstrymYmuUeeR3LqsWe4Xa5EdmMpqSwv7pWS3XeLeNn129dZNio1"
            ),
            Arguments.of(
                "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
                "ypub6Ynvx7RLNYgWzFGM8aeU43hFNjTh7u5Grrup7Ryu2nKZ1Y8FWKaJZXiUrkJSnMmGVNBoVH1DNDtQ32tR4YFDRSpSUXjjvsiMnCvoPHVWXJP"
            )
        );
    }
}
