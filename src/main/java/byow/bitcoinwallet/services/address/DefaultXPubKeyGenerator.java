package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPubTypes;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.entities.XPubTypes.DEFAULT_X_PUB;

@Component
public class DefaultXPubKeyGenerator extends AbstractXPubKeyGenerator {
    private static final DerivationPath DEFAULT_CHILD_PATH = new DerivationPath("0");

    public DefaultXPubKeyGenerator(DefaultKeyGenerator defaultKeyGenerator) {
        super(defaultKeyGenerator);
    }

    @Override
    public String generateXPubkeySerialized(String seed) {
        return serializeKey(generateDefaultXPubkey(seed), SEGWIT_X_PUB_PREFIX);
    }

    @Override
    public XPubTypes getType() {
        return DEFAULT_X_PUB;
    }

    private Object generateDefaultXPubkey(String seed) {
        return generateXPubKey(seed, SEGWIT_ROOT_PATH, DEFAULT_CHILD_PATH);
    }
}
