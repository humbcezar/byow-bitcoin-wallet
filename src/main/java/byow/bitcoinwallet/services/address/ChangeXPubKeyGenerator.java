package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPubTypes;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.entities.XPubTypes.CHANGE_X_PUB;

@Component
public class ChangeXPubKeyGenerator extends AbstractXPubKeyGenerator {
    private static final DerivationPath CHANGE_CHILD_PATH = new DerivationPath("1");

    public ChangeXPubKeyGenerator(DefaultKeyGenerator defaultKeyGenerator) {
        super(defaultKeyGenerator);
    }

    @Override
    public String generateXPubkeySerialized(String seed) {
        return serializeKey(generateChangeXPubkey(seed), SEGWIT_X_PUB_PREFIX);
    }

    @Override
    public XPubTypes getType() {
        return CHANGE_X_PUB;
    }

    private Object generateChangeXPubkey(String seed) {
        return generateXPubKey(seed, SEGWIT_ROOT_PATH, CHANGE_CHILD_PATH);
    }
}
