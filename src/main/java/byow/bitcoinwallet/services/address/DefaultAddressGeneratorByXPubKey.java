package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
public class DefaultAddressGeneratorByXPubKey extends AbstractAddressGeneratorByXPubKey {
    private final String addressPrefix;

    @Autowired
    public DefaultAddressGeneratorByXPubKey(
        DefaultKeyGenerator defaultKeyGenerator,
        @Qualifier("addressPrefix") String addressPrefix
    ) {
        super(defaultKeyGenerator);
        this.addressPrefix = addressPrefix;
    }

    public String generate(String key, DerivationPath derivationPath) {
        return bip32_key_to_addr_segwit(getPublicKey(key, derivationPath), addressPrefix, 0);
    }
}
