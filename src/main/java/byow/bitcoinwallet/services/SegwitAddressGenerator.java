package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import com.blockstream.libwally.Wally;
import org.springframework.stereotype.Component;

import static com.blockstream.libwally.Wally.*;

@Component
public class SegwitAddressGenerator implements AddressGenerator {

    public String generate(Wallet wallet) {
        Object BIP32RootKey = buildBIP32RootKey(wallet.getSeed());
        Object privateKey = getFirstBIP84AddressPrivateKey(BIP32RootKey);

        return bip32_key_to_addr_segwit(privateKey, "bc", 0);
    }

    private Object getFirstBIP84AddressPrivateKey(Object BIP32RootKey) {
        return bip32_key_from_parent_path(
                BIP32RootKey,
                new int[]{
                        BIP32_INITIAL_HARDENED_CHILD + 84,
                        BIP32_INITIAL_HARDENED_CHILD,
                        BIP32_INITIAL_HARDENED_CHILD,
                        0,
                        0
                },
                BIP32_FLAG_KEY_PRIVATE
        );
    }

    private Object buildBIP32RootKey(String seed) {
        return bip32_key_from_seed(Wally.hex_to_bytes(seed), Wally.BIP32_VER_MAIN_PRIVATE, 0);
    }
}
