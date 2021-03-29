package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPub;
import com.blockstream.libwally.Wally;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.blockstream.libwally.Wally.*;
import static wf.bitcoin.krotjson.HexCoder.decode;
import static wf.bitcoin.krotjson.HexCoder.encode;

@Component
public class DefaultKeyGenerator {
    private static final String BIP_32_PREFIX = "0488b21e";

    public Object getXPrivateKey(String seed, DerivationPath derivationPath) {
        Object BIP32RootKey = buildBIP32RootKey(seed);
        return bip32_key_from_parent_path(
            BIP32RootKey,
            derivationPath.getParsedPath(),
            BIP32_FLAG_KEY_PRIVATE
        );
    }

    public Object getXPubKey(String key, DerivationPath derivationPath) {
        String pubKeyProcessed = encode(base58_to_bytes(key));
        String pubKeyHexEncoded = BIP_32_PREFIX.concat(pubKeyProcessed.substring(8, pubKeyProcessed.length() - 8));
        Object unserializedPubKey = bip32_key_unserialize(decode(pubKeyHexEncoded));
        return bip32_key_from_parent_path(
            unserializedPubKey,
            derivationPath.getParsedPath(),
            BIP32_FLAG_KEY_PUBLIC
        );
    }

    public byte[] getPublicKeyAsByteArray(Set<XPub> xPubs, DerivationPath derivationPath) {
        XPub xPub = resolveXPub(xPubs, derivationPath);
        return bip32_key_get_pub_key(getXPubKey(xPub.getKey(), derivationPath.lastStep()));
    }

    public byte[] getPrivateKeyAsByteArray(String seed, DerivationPath derivationPath) {
        return bip32_key_get_priv_key(getXPrivateKey(seed, derivationPath));
    }

    private XPub resolveXPub(Set<XPub> xPubs, DerivationPath derivationPath) {
        return xPubs.stream()
            .filter(xPub -> xPub.getType().equals(derivationPath.getType().toString()))
            .findFirst()
            .orElseThrow();
    }

    private Object buildBIP32RootKey(String seed) {
        return bip32_key_from_seed(Wally.hex_to_bytes(seed), BIP32_VER_MAIN_PRIVATE, 0);
    }
}
