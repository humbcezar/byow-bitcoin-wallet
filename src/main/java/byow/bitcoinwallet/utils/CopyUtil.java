package byow.bitcoinwallet.utils;

import javafx.scene.input.ClipboardContent;

import static javafx.scene.input.Clipboard.getSystemClipboard;

public class CopyUtil {
    public static void copy(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        getSystemClipboard().setContent(content);
    }
}
