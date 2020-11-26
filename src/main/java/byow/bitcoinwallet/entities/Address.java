package byow.bitcoinwallet.entities;

import byow.bitcoinwallet.services.DerivationPath;

public class Address {
    private String address;

    private DerivationPath derivationPath;

    public Address(String address, DerivationPath derivationPath) {
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
