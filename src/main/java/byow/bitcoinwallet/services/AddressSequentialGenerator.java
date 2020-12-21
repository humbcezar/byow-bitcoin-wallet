package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class AddressSequentialGenerator {
    @Autowired
    private AddressGenerator addressGenerator;

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
