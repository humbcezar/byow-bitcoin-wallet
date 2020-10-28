package byow.bitcoinwallet.entities;

import javafx.scene.control.MenuItem;

public class LoadWalletMenuItem extends MenuItem {
    public LoadWalletMenuItem(String text) {
        super(text);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof String)) {
            return false;
        }
        return getText().equals(obj);
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }
}
