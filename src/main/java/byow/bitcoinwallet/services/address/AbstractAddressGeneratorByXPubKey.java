package byow.bitcoinwallet.services.address;

abstract class AbstractAddressGeneratorByXPubKey implements AddressGenerator {
    private final DefaultKeyGenerator defaultKeyGenerator;

    protected AbstractAddressGeneratorByXPubKey(DefaultKeyGenerator defaultKeyGenerator) {
        this.defaultKeyGenerator = defaultKeyGenerator;
    }

    protected Object getPublicKey(String key, DerivationPath derivationPath) {
        return defaultKeyGenerator.getXPubKey(key, derivationPath.lastStep());
    }
}
