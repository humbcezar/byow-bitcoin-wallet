package byow.bitcoinwallet.services;

import com.blockstream.libwally.Wally;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
public class DefaultKeyGenerator {
    public Object getPrivateKey(String seed, DerivationPath derivationPath) {
        Object BIP32RootKey = buildBIP32RootKey(seed);
        return bip32_key_from_parent_path(
            BIP32RootKey,
            derivationPath.getParsedPath(),
            BIP32_FLAG_KEY_PRIVATE
        );
    }

    public byte[] getPublicKeyAsByteArray(String seed, DerivationPath derivationPath) {
        return bip32_key_get_pub_key(getPrivateKey(seed, derivationPath));
    }

    public byte[] getPrivateKeyAsByteArray(String seed, DerivationPath derivationPath) {
        return bip32_key_get_priv_key(getPrivateKey(seed, derivationPath));
    }

    private Object buildBIP32RootKey(String seed) {
        return bip32_key_from_seed(Wally.hex_to_bytes(seed), BIP32_VER_MAIN_PRIVATE, 0);
    }
}
