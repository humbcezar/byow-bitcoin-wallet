package byow.bitcoinwallet.services.address;

public interface AddressGenerator {
    String generate(String key, DerivationPath derivationPath);
}
