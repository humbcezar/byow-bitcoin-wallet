package byow.bitcoinwallet.utils;

public class HexUtils {

    public static String revertEndianess(String txid) {
        StringBuilder  result = new StringBuilder();
        for (int i = 0; i <= txid.length() - 2; i = i + 2) {
            result.append(new StringBuilder(txid.substring(i, i + 2)).reverse());
        }
        return result.reverse().toString();
    }

}
