package byow.bitcoinwallet.services.address;

public interface AddressGenerator {
    String generate(String seed, DerivationPath derivationPath);
}
