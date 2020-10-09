package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;

public interface AddressGenerator {

    public String generate(Wallet wallet, DerivationPath derivationPath);

}
