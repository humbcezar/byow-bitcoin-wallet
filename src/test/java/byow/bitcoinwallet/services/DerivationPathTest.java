package byow.bitcoinwallet.services;

import byow.bitcoinwallet.services.address.DerivationPath;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.blockstream.libwally.Wally.BIP32_INITIAL_HARDENED_CHILD;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DerivationPathTest {

    @ParameterizedTest
    @MethodSource("testDerivationPathParsedPathArguments")
    public void testDerivationPathParsedPath(int[] expectedArray, String path) {
        DerivationPath derivationPath = new DerivationPath(path);
        assertArrayEquals(expectedArray, derivationPath.getParsedPath());
    }

    private static Stream<Arguments> testDerivationPathParsedPathArguments() {
        return Stream.of(
                Arguments.of(
                        new int[]{0},
                        "0"
                ),
                Arguments.of(
                        new int[]{BIP32_INITIAL_HARDENED_CHILD},
                        "0'"
                ),
                Arguments.of(
                        new int[]{0, 1},
                        "0/1"
                ),
                Arguments.of(
                        new int[]{BIP32_INITIAL_HARDENED_CHILD, 1},
                        "0'/1"
                ),
                Arguments.of(
                        new int[]{1, 0},
                        "1/0"
                ),
                Arguments.of(
                        new int[]{BIP32_INITIAL_HARDENED_CHILD + 1, 0},
                        "1'/0"
                )
        );
    }
}
