package byow.bitcoinwallet.services.address;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static byow.bitcoinwallet.services.address.DerivationPath.*;
import static com.blockstream.libwally.Wally.WALLY_ADDRESS_VERSION_P2SH_MAINNET;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestedSegwitAddressGeneratorByXPubKeyTest {

    private NestedSegwitAddressGeneratorByXPubKey nestedSegwitAddressGeneratorByXPubKey;

    @BeforeEach
    public void setup() {
        nestedSegwitAddressGeneratorByXPubKey = new NestedSegwitAddressGeneratorByXPubKey(
            new DefaultKeyGenerator(),
            WALLY_ADDRESS_VERSION_P2SH_MAINNET
        );
    }

    @ParameterizedTest
    @MethodSource("createTestCases")
    public void generate(String xPubKey, DerivationPath derivationPath, String expectedAddress) {
        String address = nestedSegwitAddressGeneratorByXPubKey.generate(xPubKey, derivationPath);
        assertEquals(expectedAddress, address);
    }

    private static Stream<Arguments> createTestCases() {
        return Stream.of(
            Arguments.of(
                "ypub6ZZmQ6SHboK63PPSbViih3nVmqqxN3p97waJZMw7wuoBb3pMeouPQvFNBP8ZwrxwmHSrbC64PJuiu21JGkWJu8BVv44PKtL9FHwi8gnWNSX",
                FIRST_BIP49_ADDRESS_PATH,
                "38opXXGrGkJzFFDtFdu2pnyH32iaapn2Ln"
            ),
            Arguments.of(
                "ypub6ZZmQ6SHboK63PPSbViih3nVmqqxN3p97waJZMw7wuoBb3pMeouPQvFNBP8ZwrxwmHSrbC64PJuiu21JGkWJu8BVv44PKtL9FHwi8gnWNSX",
                FIRST_BIP49_ADDRESS_PATH.next(1),
                "392nhoyyKNiZrDp6FbiJFLqMu16WRDLpUW"
            ),
            Arguments.of(
                "ypub6Ynvx7RLNYgWzFGM8aeU43hFNjTh7u5Grrup7Ryu2nKZ1Y8FWKaJZXiUrkJSnMmGVNBoVH1DNDtQ32tR4YFDRSpSUXjjvsiMnCvoPHVWXJP",
                FIRST_BIP49_ADDRESS_PATH,
                "37VucYSaXLCAsxYyAPfbSi9eh4iEcbShgf"
            ),
            Arguments.of(
                "ypub6Ynvx7RLNYgWzFGM8aeU43hFNjTh7u5Grrup7Ryu2nKZ1Y8FWKaJZXiUrkJSnMmGVNBoVH1DNDtQ32tR4YFDRSpSUXjjvsiMnCvoPHVWXJP",
                FIRST_BIP49_ADDRESS_PATH.next(1),
                "3LtMnn87fqUeHBUG414p9CWwnoV6E2pNKS"
            ),
            Arguments.of(
                "ypub6Ynvx7RLNYgX2yTF9GS9Eb1Wb444qVB62cxDpQF1ixXUHNMaDUvY67zpnwo2CMZXCrHtaEKHYQ4bqEKefq4R5kqUFhfRvMCn1TuQ5yJJfr2",
                FIRST_BIP49_CHANGE_PATH,
                "34K56kSjgUCUSD8GTtuF7c9Zzwokbs6uZ7"
            ),
            Arguments.of(
                "ypub6Ynvx7RLNYgX2yTF9GS9Eb1Wb444qVB62cxDpQF1ixXUHNMaDUvY67zpnwo2CMZXCrHtaEKHYQ4bqEKefq4R5kqUFhfRvMCn1TuQ5yJJfr2",
                FIRST_BIP49_CHANGE_PATH.next(1),
                "3516F2wmK51jVRrggEJsTUBNWMSLLjzvJ2"
            )
        );
    }
}