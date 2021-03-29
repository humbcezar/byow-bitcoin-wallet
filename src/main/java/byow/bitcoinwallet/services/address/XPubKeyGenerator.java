package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPubTypes;

public interface XPubKeyGenerator {
    String generateXPubkeySerialized(String seed);

    XPubTypes getType();
}
