package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
@Primary
public class DefaultAddressGeneratorBySeed implements AddressGenerator {
    private final DefaultKeyGenerator defaultKeyGenerator;

    private final String addressPrefix;

    @Autowired
    public DefaultAddressGeneratorBySeed(
        DefaultKeyGenerator defaultKeyGenerator,
        @Qualifier("addressPrefix") String addressPrefix
    ) {
        this.defaultKeyGenerator = defaultKeyGenerator;
        this.addressPrefix = addressPrefix;
    }

    public String generate(String seed, DerivationPath derivationPath) {
        Object privateKey = defaultKeyGenerator.getXPrivateKey(seed, derivationPath);
        return bip32_key_to_addr_segwit(privateKey, addressPrefix, 0);
    }
}
