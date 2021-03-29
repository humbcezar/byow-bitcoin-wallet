package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPubTypes;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.entities.XPubTypes.CHANGE_NESTED_SEGWIT_X_PUB;

@Component
public class ChangeNestedSegwitXPubKeyGenerator extends AbstractXPubKeyGenerator{

    private static final DerivationPath CHANGE_NESTED_SEGWIT_CHILD_PATH = new DerivationPath("1");

    public ChangeNestedSegwitXPubKeyGenerator(DefaultKeyGenerator defaultKeyGenerator) {
        super(defaultKeyGenerator);
    }

    @Override
    public String generateXPubkeySerialized(String seed) {
        return serializeKey(generateDefaultXPubkey(seed), NESTED_SEGWIT_X_PUB_PREFIX);
    }

    @Override
    public XPubTypes getType() {
        return CHANGE_NESTED_SEGWIT_X_PUB;
    }

    private Object generateDefaultXPubkey(String seed) {
        return generateXPubKey(seed, NESTED_SEGWIT_ROOT_PATH, CHANGE_NESTED_SEGWIT_CHILD_PATH);
    }
}
