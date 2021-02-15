package byow.bitcoinwallet.services.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
@Primary
public class DefaultAddressGenerator implements AddressGenerator {
    private DefaultKeyGenerator defaultKeyGenerator;

    private String addressPrefix;

    @Autowired
    public DefaultAddressGenerator(
        DefaultKeyGenerator defaultKeyGenerator,
        @Qualifier("addressPrefix") String addressPrefix
    ) {
        this.defaultKeyGenerator = defaultKeyGenerator;
        this.addressPrefix = addressPrefix;
    }

    public String generate(String seed, DerivationPath derivationPath) {
        Object privateKey = defaultKeyGenerator.getPrivateKey(seed, derivationPath);
        return bip32_key_to_addr_segwit(privateKey, addressPrefix, 0);
    }
}
