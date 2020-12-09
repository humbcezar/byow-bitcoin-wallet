package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.guitests.TestBase;
import byow.bitcoinwallet.utils.UnspentUtil;
import com.blockstream.libwally.Wally;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.lang.String.format;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletUpdaterTest extends TestBase {

    @MockBean
    private MultiAddressesImporter multiAddressesImporter;

    @MockBean
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private DefaultAddressGenerator addressGenerator;

    @Autowired
    private CurrentAddressesManager currentAddressesManager;

    @Autowired
    private NextReceivingAddress nextReceivingAddress;

    @Autowired
    private UnspentUtil unspentUtil;

    private SeedGenerator seedGenerator = new SeedGenerator(Wally.bip39_get_wordlist(Languages.EN), new EntropyCreator());

    private WalletUpdater walletUpdater;


    @BeforeEach
    void setUp() {
        currentAddressesManager.clear();
        walletUpdater = new WalletUpdater(currentAddressesManager);
    }

    @Test
    void updateWithZeroUsedAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(new ArrayList<>());

        Date date = new Date();

        walletUpdater.setInitialAddressToMonitor(20);
        walletUpdater.setDate(date).setSeed(seed).updateReceivingAddresses();

        verify(multiAddressesImporter).importMultiAddresses(date, expectedAddresses);
        assertTrue(
             nextReceivingAddress.getValue().getAddress().equals(expectedAddresses[0]) &&
            nextReceivingAddress.getValue().getConfirmations() == 0 &&
            nextReceivingAddress.getValue().getBalance().equals("0")
        );
        assertEquals(20, currentAddressesManager.getReceivingAddresses().size());
    }

    @Test
    void updateWithFirstUsedAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        List<Unspent> unspents = List.of(unspentUtil.unspent(expectedAddresses[0], TEN, 0));
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(unspents);

        Date date = new Date();

        walletUpdater.setInitialAddressToMonitor(20);
        walletUpdater.setDate(date).setSeed(seed).updateReceivingAddresses();

        verify(multiAddressesImporter).importMultiAddresses(date, expectedAddresses);
        assertTrue(
            nextReceivingAddress.getValue().getAddress().equals(expectedAddresses[1]) &&
            nextReceivingAddress.getValue().getConfirmations() == 0 &&
            nextReceivingAddress.getValue().getBalance().equals("0")
        );
        assertEquals(20, currentAddressesManager.getReceivingAddresses().size());
        FilteredList<ReceivingAddress> usedReceivingAddresses = currentAddressesManager
                .getReceivingAddresses().filtered(receivingAddress ->
            receivingAddress.getBigDecimalBalance().compareTo(BigDecimal.ZERO) > 0
        );
        assertEquals(1, usedReceivingAddresses.size());
        IntStream.range(0, usedReceivingAddresses.size()).forEach(i -> {
            assertTrue(
                 usedReceivingAddresses.get(i).getBalance().equals(unspents.get(i).amount().toString()) &&
                usedReceivingAddresses.get(i).getConfirmations() == unspents.get(i).confirmations() &&
                usedReceivingAddresses.get(i).getAddress().equals(unspents.get(i).address())
            );
        });
    }

    @Test
    void updateWithFirstFiveUsedAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        List<Unspent> unspents = List.of(
                unspentUtil.unspent(expectedAddresses[0], TEN, 1),
                unspentUtil.unspent(expectedAddresses[1], TEN, 1),
                unspentUtil.unspent(expectedAddresses[2], TEN, 1),
                unspentUtil.unspent(expectedAddresses[3], TEN, 1),
                unspentUtil.unspent(expectedAddresses[4], TEN, 1)
        );
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(unspents);

        Date date = new Date();

        walletUpdater.setInitialAddressToMonitor(20);
        walletUpdater.setDate(date).setSeed(seed).updateReceivingAddresses();

        verify(multiAddressesImporter).importMultiAddresses(date, expectedAddresses);
        assertTrue(
             nextReceivingAddress.getValue().getAddress().equals(expectedAddresses[5]) &&
            nextReceivingAddress.getValue().getConfirmations() == 0 &&
            nextReceivingAddress.getValue().getBalance().equals("0")
        );
        assertEquals(20, currentAddressesManager.getReceivingAddresses().size());
        FilteredList<ReceivingAddress> usedReceivingAddresses = currentAddressesManager
                .getReceivingAddresses().filtered(receivingAddress ->
                        receivingAddress.getBigDecimalBalance().compareTo(BigDecimal.ZERO) > 0
                );
        assertEquals(5, usedReceivingAddresses.size());
        IntStream.range(0, usedReceivingAddresses.size()).forEach(i -> {
            assertTrue(
                 usedReceivingAddresses.get(i).getBalance().equals(unspents.get(i).amount().toString()) &&
                usedReceivingAddresses.get(i).getConfirmations() == unspents.get(i).confirmations() &&
                usedReceivingAddresses.get(i).getAddress().equals(unspents.get(i).address())
            );
        });
    }

    @Test
    void updateWithFirstFiveUsedAddressesWithThreeUtxosToOneAddress() {
        ObservableList<ReceivingAddress> receivingAddresses = (ObservableList<ReceivingAddress>) mock(ObservableList.class);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        List<Unspent> unspents = List.of(
                unspentUtil.unspent(expectedAddresses[0], TEN, 1),
                unspentUtil.unspent(expectedAddresses[1], TEN, 1),
                unspentUtil.unspent(expectedAddresses[2], TEN, 1),
                unspentUtil.unspent(expectedAddresses[3], TEN, 1),
                unspentUtil.unspent(expectedAddresses[4], TEN, 1),
                unspentUtil.unspent(expectedAddresses[4], new BigDecimal(1), 0),
                unspentUtil.unspent(expectedAddresses[4], new BigDecimal(2), 1)
        );
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(unspents);

        Date date = new Date();

        walletUpdater.setInitialAddressToMonitor(20);
        walletUpdater.setDate(date).setSeed(seed).updateReceivingAddresses();

        verify(multiAddressesImporter).importMultiAddresses(date, expectedAddresses);
        assertTrue(
             nextReceivingAddress.getValue().getAddress().equals(expectedAddresses[5]) &&
            nextReceivingAddress.getValue().getConfirmations() == 0 &&
            nextReceivingAddress.getValue().getBalance().equals("0")
        );
        assertEquals(20, currentAddressesManager.getReceivingAddresses().size());
        FilteredList<ReceivingAddress> usedReceivingAddresses = currentAddressesManager
                .getReceivingAddresses().filtered(receivingAddress ->
                        receivingAddress.getBigDecimalBalance().compareTo(BigDecimal.ZERO) > 0
                );
        assertEquals(5, usedReceivingAddresses.size());
        IntStream.range(0, usedReceivingAddresses.size() - 1).forEach(i -> {
            assertTrue(
                  usedReceivingAddresses.get(i).getBalance().equals(unspents.get(i).amount().toString()) &&
                usedReceivingAddresses.get(i).getConfirmations() == unspents.get(i).confirmations() &&
                usedReceivingAddresses.get(i).getAddress().equals(unspents.get(i).address())
            );
        });
        assertTrue(
            usedReceivingAddresses.get(4).getBalance().equals("13") &&
            usedReceivingAddresses.get(4).getConfirmations() == 0 &&
            usedReceivingAddresses.get(4).getAddress().equals(unspents.get(4).address())
        );
    }

    @Test
    void updateWithTwentyUsedAddresses() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        String[] expectedAddresses2 = expectedAddresses(seed, 20, new DerivationPath("84'/0'/0'/0/20"));
        List<Unspent> unspents = IntStream.range(0, 20)
                .mapToObj(i -> unspentUtil.unspent(expectedAddresses[i], TEN, 1))
                .collect(Collectors.toList());
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses)).thenReturn(unspents);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses2)).thenReturn(new ArrayList<>());

        Date date = new Date();

        walletUpdater.setInitialAddressToMonitor(20);
        walletUpdater.setDate(date).setSeed(seed).updateReceivingAddresses();

        ArgumentCaptor<String[]> expectedAddressesCaptured = ArgumentCaptor.forClass(String[].class);
        ArgumentCaptor<Date> expectedDateCaptured = ArgumentCaptor.forClass(Date.class);
        verify(multiAddressesImporter, times(2)).importMultiAddresses(expectedDateCaptured.capture(), expectedAddressesCaptured.capture());
        List<String[]> allExpectedAddresses = expectedAddressesCaptured.getAllValues();
        assertEquals(40, new HashSet<>(allExpectedAddresses).size());
        assertArrayEquals(allExpectedAddresses.subList(0, 20).toArray(), expectedAddresses);
        assertArrayEquals(allExpectedAddresses.subList(20, 40).toArray(), expectedAddresses2);
        List<Date> allDateValues = expectedDateCaptured.getAllValues();
        assertEquals(allDateValues.get(0), date);

        assertEquals(40, currentAddressesManager.getReceivingAddresses().size());
        FilteredList<ReceivingAddress> usedReceivingAddresses = currentAddressesManager
                .getReceivingAddresses().filtered(receivingAddress ->
                        receivingAddress.getBigDecimalBalance().compareTo(BigDecimal.ZERO) > 0
                );
        assertEquals(20, usedReceivingAddresses.size());
        IntStream.range(0, usedReceivingAddresses.size()).forEach(i -> {
            assertTrue(
                usedReceivingAddresses.get(i).getBalance().equals(unspents.get(i).amount().toString()) &&
                usedReceivingAddresses.get(i).getConfirmations() == unspents.get(i).confirmations() &&
                usedReceivingAddresses.get(i).getAddress().equals(unspents.get(i).address())
            );
        });

        String expectedAddress = expectedAddresses(seed, 1, new DerivationPath("84'/0'/0'/0/20"))[0];

        assertTrue(
            nextReceivingAddress.getValue().getAddress().equals(expectedAddress) &&
            nextReceivingAddress.getValue().getConfirmations() == 0 &&
            nextReceivingAddress.getValue().getBalance().equals("0"),
            format("actual address: %s, expected address: %s", nextReceivingAddress.getValue().getAddress(), expectedAddress)
                .concat(
                    format("\nactual confirmations: %s, expected confirmations: %s",
                            nextReceivingAddress.getValue().getConfirmations(),
                            0
                    )
                ).concat(
                    format("\nactual balance: %s, expected balance: %s",
                            nextReceivingAddress.getValue().getBalance(),
                            "0"
                    )
                )
        );
    }

    private String[] expectedAddresses(String seed, int numberOfAddresses, DerivationPath initialDerivationPath) {
        List<String> addressList = new LinkedList<>();
        DerivationPath addressPath = initialDerivationPath;
        for (int i = 0; i < numberOfAddresses; i++) {
            addressList.add(addressGenerator.generate(seed, addressPath));
            addressPath = addressPath.next(1);
        }
        return addressList.toArray(new String[0]);
    }
}