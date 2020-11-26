package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AddressSequentialGeneratorTest {
    @Autowired
    AddressSequentialGenerator addressSequentialGenerator;

    @Autowired
    SeedGenerator seedGenerator;

    @Autowired
    DefaultAddressGenerator addressGenerator;

    @Test
    public void deriveAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        List<String> addresses = addressSequentialGenerator.deriveAddresses(
                5,
                seed,
                FIRST_BIP84_ADDRESS_PATH
        ).stream().map(Address::getAddress).collect(Collectors.toList());
        assertArrayEquals(expectedAddresses(seed, 5), addresses.toArray());
    }

    private String[] expectedAddresses(String seed, int numberOfAddresses) {
        List<String> addressList = new LinkedList<>();
        DerivationPath addressPath = FIRST_BIP84_ADDRESS_PATH;
        for (int i = 0; i < numberOfAddresses; i++) {
            addressList.add(addressGenerator.generate(seed, addressPath));
            addressPath = addressPath.next(1);
        }
        return addressList.toArray(new String[0]);
    }
}