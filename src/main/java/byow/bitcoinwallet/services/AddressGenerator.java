package byow.bitcoinwallet.services;

public interface AddressGenerator {
    String generate(String seed, DerivationPath derivationPath);
}
