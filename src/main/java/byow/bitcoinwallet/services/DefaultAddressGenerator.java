package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import com.blockstream.libwally.Wally;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
public class DefaultAddressGenerator implements AddressGenerator {

    public String generate(Wallet wallet, DerivationPath derivationPath) {
        Object BIP32RootKey = buildBIP32RootKey(wallet.getSeed());
        Object privateKey = getPrivateKey(BIP32RootKey, derivationPath);

        return bip32_key_to_addr_segwit(privateKey, "bc", 0);
    }

    private Object getPrivateKey(Object BIP32RootKey, DerivationPath derivationPath) {
        return bip32_key_from_parent_path(
                BIP32RootKey,
                derivationPath.getParsedPath(),
                BIP32_FLAG_KEY_PRIVATE
        );
    }

    private Object buildBIP32RootKey(String seed) {
        return bip32_key_from_seed(Wally.hex_to_bytes(seed), Wally.BIP32_VER_MAIN_PRIVATE, 0);
    }
}
