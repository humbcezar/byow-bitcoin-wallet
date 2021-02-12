package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;

public interface AddressUpdater {
    void update(Wallet wallet);
}
