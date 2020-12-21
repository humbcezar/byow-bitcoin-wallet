package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Address;

import java.util.LinkedList;
import java.util.List;

public class AddressSequentialGenerator {
    private AddressGenerator addressGenerator;

    public AddressSequentialGenerator(AddressGenerator addressGenerator) {
        this.addressGenerator = addressGenerator;
    }

    public List<Address> deriveAddresses(
        int numberOfAddresses,
        String seed,
        DerivationPath firstDerivationPath
    ) {
        List<Address> addressList = new LinkedList<>();
        DerivationPath addressPath = firstDerivationPath;
        for (int i = 0; i < numberOfAddresses; i++) {
            addressList.add(new Address(addressGenerator.generate(seed, addressPath), addressPath));
            addressPath = addressPath.next(1);
        }
        return addressList;
    }
}
