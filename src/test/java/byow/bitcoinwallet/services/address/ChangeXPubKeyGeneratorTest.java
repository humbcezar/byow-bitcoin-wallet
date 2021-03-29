package byow.bitcoinwallet.services.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeXPubKeyGeneratorTest {

    private SeedGenerator seedGenerator;

    private ChangeXPubKeyGenerator changeXPubKeyGenerator;

    @BeforeEach
    public void setup() {
        seedGenerator = new SeedGenerator();
        changeXPubKeyGenerator = new ChangeXPubKeyGenerator(new DefaultKeyGenerator());
    }

    @ParameterizedTest
    @MethodSource("testCreateChangeXPubTestCases")
    public void testCreateChangeXPub(String mnemonicSeed, String expectedXPub) {
        String seed = seedGenerator.generateSeedAsString(mnemonicSeed, "");
        assertEquals(expectedXPub, changeXPubKeyGenerator.generateXPubkeySerialized(seed));
    }

    private static Stream<Arguments> testCreateChangeXPubTestCases() {
        return Stream.of(
            Arguments.of(
                "deliver shift mean disorder bacon echo destroy route distance omit speed vague where enact cereal",
                "zpub6toULrhXxEDAXg2dcCxTN1KKnWFnmmSfQfP2iodQikGEPEgNW18Z2Y4Edw4pdVc2hA2oRrfbtR3tsmE39jUzvh4dw6aVZHeTVo3VCTHNN2r"
            ),
            Arguments.of(
                "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about",
                "zpub6u4KbU8TSgNuco8HzL1LqM2ePjv8wrxUKENTtfambyxBbACZg5qvqqzAPwwAopTuxkrQzs661k5A6Q1P8b25a9DDJXYXDpN4KPwxygrx9Py"
            )
        );
    }
}