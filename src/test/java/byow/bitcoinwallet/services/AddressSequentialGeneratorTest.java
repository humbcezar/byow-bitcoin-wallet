package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.DerivationPath.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AddressSequentialGeneratorTest {
    @Autowired
    AddressSequentialGenerator defaultAddressSequentialGenerator;

    @Autowired
    @Qualifier("nestedSegwitAddressSequentialGenerator")
    AddressSequentialGenerator nestedSegwitAddressSequentialGenerator;

    @Autowired
    SeedGenerator seedGenerator;

    @Autowired
    DefaultAddressGenerator defaultAddressGenerator;

    @Autowired
    NestedSegwitAddressGenerator nestedSegwitAddressGenerator;

    @Test
    public void deriveDefaultAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        List<String> addresses = defaultAddressSequentialGenerator.deriveAddresses(
                5,
                seed,
                FIRST_BIP84_ADDRESS_PATH
        ).stream().map(Address::getAddress).collect(Collectors.toList());
        assertArrayEquals(expectedAddresses(seed, 5, FIRST_BIP84_ADDRESS_PATH, defaultAddressGenerator), addresses.toArray());
    }

    @Test
    public void deriveNestedSegwitAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        List<String> addresses = nestedSegwitAddressSequentialGenerator.deriveAddresses(
                5,
                seed,
                FIRST_BIP49_ADDRESS_PATH
        ).stream().map(Address::getAddress).collect(Collectors.toList());
        assertArrayEquals(expectedAddresses(seed, 5, FIRST_BIP49_ADDRESS_PATH, nestedSegwitAddressGenerator), addresses.toArray());
    }

    private String[] expectedAddresses(String seed, int numberOfAddresses, DerivationPath derivationPath, AddressGenerator addressGenerator) {
        List<String> addressList = new LinkedList<>();
        for (int i = 0; i < numberOfAddresses; i++) {
            addressList.add(addressGenerator.generate(seed, derivationPath));
            derivationPath = derivationPath.next(1);
        }
        return addressList.toArray(new String[0]);
    }
}