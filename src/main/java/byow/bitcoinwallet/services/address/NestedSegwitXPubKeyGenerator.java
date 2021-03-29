package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPubTypes;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.entities.XPubTypes.NESTED_SEGWIT_X_PUB;

@Component
public class NestedSegwitXPubKeyGenerator extends AbstractXPubKeyGenerator {

    private static final DerivationPath NESTED_SEGWIT_CHILD_PATH = new DerivationPath("0");

    public NestedSegwitXPubKeyGenerator(DefaultKeyGenerator defaultKeyGenerator) {
        super(defaultKeyGenerator);
    }

    @Override
    public String generateXPubkeySerialized(String seed) {
        return serializeKey(generateDefaultXPubkey(seed), NESTED_SEGWIT_X_PUB_PREFIX);
    }

    @Override
    public XPubTypes getType() {
        return NESTED_SEGWIT_X_PUB;
    }

    private Object generateDefaultXPubkey(String seed) {
        return generateXPubKey(seed, NESTED_SEGWIT_ROOT_PATH, NESTED_SEGWIT_CHILD_PATH);
    }
}
