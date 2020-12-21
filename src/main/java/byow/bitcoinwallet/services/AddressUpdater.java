package byow.bitcoinwallet.services;

import java.util.Date;

public interface AddressUpdater {
    void update(String seed, Date walletCreationDate);
}
