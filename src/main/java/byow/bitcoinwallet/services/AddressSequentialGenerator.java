package byow.bitcoinwallet.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class AddressSequentialGenerator {
    @Autowired
    private AddressGenerator addressGenerator;

    public List<String> deriveAddresses(
            int numberOfAddresses,
            String seed,
            DerivationPath firstDerivationPath
    ) {
        List<String> addressList = new LinkedList<>();
        DerivationPath addressPath = firstDerivationPath;
        for (int i = 0; i < numberOfAddresses; i++) {
            addressList.add(addressGenerator.generate(seed, addressPath));
            addressPath = addressPath.next(1);
        }
        return addressList;
    }
}
