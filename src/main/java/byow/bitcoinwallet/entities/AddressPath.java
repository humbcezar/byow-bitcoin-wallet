package byow.bitcoinwallet.entities;

import byow.bitcoinwallet.services.address.DerivationPath;

public class AddressPath {
    private String address;

    private DerivationPath derivationPath;

    public AddressPath(String address, DerivationPath derivationPath) {
        this.address = address;
        this.derivationPath = derivationPath;
    }

    public String getAddress() {
        return address;
    }

    public DerivationPath getDerivationPath() {
        return derivationPath;
    }
}
