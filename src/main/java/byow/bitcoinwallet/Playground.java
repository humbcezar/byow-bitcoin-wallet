package byow.bitcoinwallet;

import com.blockstream.libwally.Wally;
import java.security.SecureRandom;


public class Playground {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void playground(String[] args) {
        System.out.println(Wally.isEnabled());
        final String langEn = Wally.bip39_get_languages().split(" ")[0];
        Object wordList = Wally.bip39_get_wordlist(langEn);
        final byte[] entropy = new byte[Wally.BIP32_ENTROPY_LEN_128];
        secureRandom.nextBytes(entropy);
        String mnemonicSeed = Wally.bip39_mnemonic_from_bytes(wordList, entropy);
        System.out.println(mnemonicSeed);
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
            System.out.println(true);
        } catch (final Exception e) {
            System.out.println(false);
        }
        final byte[] seed = new byte[Wally.BIP39_SEED_LEN_512];
        Wally.bip39_mnemonic_to_seed(mnemonicSeed, "", seed);
        System.out.println(Wally.hex_from_bytes(seed));

        final Object seedKey = Wally.bip32_key_from_seed(seed, Wally.BIP32_VER_MAIN_PRIVATE, 0);
        System.out.println(Wally.bip32_key_to_base58(seedKey, Wally.BIP32_FLAG_KEY_PRIVATE));

        final Object derivedKey = Wally.bip32_key_from_parent(seedKey, 0, Wally.BIP32_FLAG_KEY_PRIVATE);
        final Object derivedKey2 = Wally.bip32_key_from_parent(derivedKey, 0, Wally.BIP32_FLAG_KEY_PRIVATE);
        String address = Wally.bip32_key_to_address(derivedKey2, Wally.WALLY_ADDRESS_TYPE_P2PKH, Wally.WALLY_ADDRESS_VERSION_P2PKH_MAINNET);
        System.out.println(address);
    }
}
