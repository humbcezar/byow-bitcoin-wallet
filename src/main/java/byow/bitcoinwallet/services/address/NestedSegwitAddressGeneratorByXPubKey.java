package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.WALLY_ADDRESS_TYPE_P2SH_P2WPKH;
import static com.blockstream.libwally.Wally.bip32_key_to_address;

@Component
public class NestedSegwitAddressGeneratorByXPubKey extends AbstractAddressGeneratorByXPubKey {
    private final int nestedAddressVersion;

    public NestedSegwitAddressGeneratorByXPubKey(
        DefaultKeyGenerator defaultKeyGenerator,
        @Qualifier("nestedAddressVersion") int nestedAddressVersion
    ) {
        super(defaultKeyGenerator);
        this.nestedAddressVersion = nestedAddressVersion;
    }

    @Override
    public String generate(String key, DerivationPath derivationPath) {
        return bip32_key_to_address(getPublicKey(key, derivationPath), WALLY_ADDRESS_TYPE_P2SH_P2WPKH, nestedAddressVersion);
    }
}
