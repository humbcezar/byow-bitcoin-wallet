package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.AddressPath;

import java.util.LinkedList;
import java.util.List;

public class AddressSequentialGenerator {
    private AddressGenerator addressGenerator;

    public AddressSequentialGenerator(AddressGenerator addressGenerator) {
        this.addressGenerator = addressGenerator;
    }

    public List<AddressPath> deriveAddresses(
        int numberOfAddresses,
        String seed,
        DerivationPath firstDerivationPath
    ) {
        List<AddressPath> addressList = new LinkedList<>();
        DerivationPath addressPath = firstDerivationPath;
        for (int i = 0; i < numberOfAddresses; i++) {
            addressList.add(new AddressPath(addressGenerator.generate(seed, addressPath), addressPath));
            addressPath = addressPath.next(1);
        }
        return addressList;
    }
}
