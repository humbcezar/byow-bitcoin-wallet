package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.XPubTypes;

import java.util.Arrays;

import static byow.bitcoinwallet.entities.XPubTypes.*;
import static com.blockstream.libwally.Wally.BIP32_INITIAL_HARDENED_CHILD;

public class DerivationPath {

    public static final DerivationPath FIRST_BIP84_ADDRESS_PATH = new DerivationPath("84'/0'/0'/0/0");

    public static final DerivationPath FIRST_BIP84_CHANGE_PATH = new DerivationPath("84'/0'/0'/1/0");

    public static final DerivationPath FIRST_BIP49_ADDRESS_PATH = new DerivationPath("49'/0'/0'/0/0");

    public static final DerivationPath FIRST_BIP49_CHANGE_PATH = new DerivationPath("49'/0'/0'/1/0");

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

    public DerivationPath next(int n) {
        String[] splitPath = path.split("/");
        int nextLast = Integer.parseInt(splitPath[splitPath.length - 1]) + n;
        splitPath[splitPath.length - 1] = Integer.toString(nextLast);

        return new DerivationPath(String.join("/", splitPath));
    }

    public DerivationPath lastStep() {
        String[] splitPath = path.split("/");
        return new DerivationPath(splitPath[splitPath.length - 1]);
    }

    public XPubTypes getType() {
        String[] splitPath = path.split("/");
        if (splitPath.length < 5) {
            return UNKNOWN;
        }
        if (splitPath[0].equals("84'") && splitPath[3].equals("0")) {
            return DEFAULT_X_PUB;
        }
        if (splitPath[0].equals("84'") && splitPath[3].equals("1")) {
            return CHANGE_X_PUB;
        }
        if (splitPath[0].equals("49'") && splitPath[3].equals("0")) {
            return NESTED_SEGWIT_X_PUB;
        }
        if (splitPath[0].equals("49'") && splitPath[3].equals("1")) {
            return CHANGE_NESTED_SEGWIT_X_PUB;
        }
        return UNKNOWN;
    }
}
