package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
public class NestedSegwitAddressGeneratorBySeed implements AddressGenerator {
    private final int nestedAddressVersion;

    private final DefaultKeyGenerator defaultKeyGenerator;

    @Autowired
    public NestedSegwitAddressGeneratorBySeed(
        DefaultKeyGenerator defaultKeyGenerator,
        @Qualifier("nestedAddressVersion") int nestedAddressVersion
    ) {
        this.defaultKeyGenerator = defaultKeyGenerator;
        this.nestedAddressVersion = nestedAddressVersion;
    }

    @Override
    public String generate(String seed, DerivationPath derivationPath) {
        Object privateKey = defaultKeyGenerator.getXPrivateKey(seed, derivationPath);
        return bip32_key_to_address(privateKey, WALLY_ADDRESS_TYPE_P2SH_P2WPKH , nestedAddressVersion);
    }
}
