package byow.bitcoinwallet.services.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeNestedSegwitXPubKeyGeneratorTest {
    private SeedGenerator seedGenerator;

    private ChangeNestedSegwitXPubKeyGenerator changeNestedSegwitXPubKeyGenerator;

    @BeforeEach
    public void setup() {
        seedGenerator = new SeedGenerator();
        changeNestedSegwitXPubKeyGenerator = new ChangeNestedSegwitXPubKeyGenerator(new DefaultKeyGenerator());
    }

    @ParameterizedTest
    @MethodSource("testCreateChangeNestedSegwitXPubTestCases")
    public void testCreateChangeNestedSegwitXPub(String mnemonicSeed, String expectedXPub) {
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        assertEquals(expectedXPub, changeNestedSegwitXPubKeyGenerator.generateXPubkeySerialized(seed));
    }

    private static Stream<Arguments> testCreateChangeNestedSegwitXPubTestCases() {
        return Stream.of(
            Arguments.of(
                "patch cabin cook faint home reveal thing try second night insect pelican inherit forest broom",
                "ypub6ZijzPoQ6tpHTyXSmFkd4aPf9wvk6iXFdgvJ2dywWYm9yR8fyssnDhZAnTMA3gqgVfScUtAcXMLBas5EzXg8SXn2zstN9bByYXhd4WVemZH"
            ),
            Arguments.of(
                "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
                "ypub6Ynvx7RLNYgX2yTF9GS9Eb1Wb444qVB62cxDpQF1ixXUHNMaDUvY67zpnwo2CMZXCrHtaEKHYQ4bqEKefq4R5kqUFhfRvMCn1TuQ5yJJfr2"
            )
        );
    }
}
