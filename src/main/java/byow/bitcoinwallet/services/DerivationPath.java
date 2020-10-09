package byow.bitcoinwallet.services;

import java.util.Arrays;

import static com.blockstream.libwally.Wally.BIP32_INITIAL_HARDENED_CHILD;

public class DerivationPath {

    public static final DerivationPath FIRST_BIP84_ADDRESS_PATH = new DerivationPath("84'/0'/0'/0/0");

    private String path;

    public DerivationPath() {
    }

    public DerivationPath(String path) {
        this.path = path;
    }

    public int[] getParsedPath() {
        return Arrays.stream(path.split("/"))
                .mapToInt(i -> {
                    if (i.contains("'")) {
                        return BIP32_INITIAL_HARDENED_CHILD + Integer.parseInt(i.replace("'", ""));
                    }
                    return Integer.parseInt(i);
                })
                .toArray();
    }
}
