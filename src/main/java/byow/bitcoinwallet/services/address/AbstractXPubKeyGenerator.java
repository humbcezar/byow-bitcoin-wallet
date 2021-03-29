package byow.bitcoinwallet.services.address;

import static com.blockstream.libwally.Wally.*;
import static com.blockstream.libwally.Wally.BASE58_FLAG_CHECKSUM;
import static wf.bitcoin.krotjson.HexCoder.decode;
import static wf.bitcoin.krotjson.HexCoder.encode;

abstract public class AbstractXPubKeyGenerator implements XPubKeyGenerator {
    protected final DefaultKeyGenerator defaultKeyGenerator;

    protected static final DerivationPath SEGWIT_ROOT_PATH = new DerivationPath("84'/0'/0'");

    protected static final DerivationPath NESTED_SEGWIT_ROOT_PATH = new DerivationPath("49'/0'/0'");

    protected static final String SEGWIT_X_PUB_PREFIX = "04b24746";

    protected static final String NESTED_SEGWIT_X_PUB_PREFIX = "049d7cb2";

    public AbstractXPubKeyGenerator(DefaultKeyGenerator defaultKeyGenerator) {
        this.defaultKeyGenerator = defaultKeyGenerator;
    }

    protected Object generateXPubKey(String seed, DerivationPath rootPath, DerivationPath childPath) {
        return bip32_key_from_parent_path(
            defaultKeyGenerator.getXPrivateKey(seed, rootPath),
            childPath.getParsedPath(),
            BIP32_FLAG_KEY_PUBLIC
        );
    }

    protected String serializeKey(Object xPubkey, String prefix) {
        String pubkeyHexEncoded = encode(bip32_key_serialize(xPubkey, BIP32_FLAG_KEY_PUBLIC));
        String pubkeyProcessed = prefix.concat(pubkeyHexEncoded.substring(8));
        return base58_from_bytes(decode(pubkeyProcessed), BASE58_FLAG_CHECKSUM);
    }
}
