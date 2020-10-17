package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.CurrentReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.services.*;
import com.blockstream.libwally.Wally;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@Import({DefaultAddressGenerator.class, AddressSequentialGenerator.class})
@ActiveProfiles("test")
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateCurrentWalletTaskTest {

    @Mock
    private MultiAddressesImporter multiAddressesImporter;

    @Mock
    private BitcoindRpcClient bitcoindRpcClient;

    @Mock
    private UpdateCurrentWalletTaskBuilder taskBuilder;

    @Autowired
    private DefaultAddressGenerator addressGenerator;

    @Autowired
    private AddressSequentialGenerator addressSequentialGenerator;

    private SeedGenerator seedGenerator = new SeedGenerator(Wally.bip39_get_wordlist(Languages.EN), new EntropyCreator());

    @Captor
    private ArgumentCaptor<Collection<ReceivingAddress>> receivingAddressesCaptured;

    @Test
    void updateWithZeroUsedAddresses() {
        UpdateCurrentWalletTask updateCurrentWalletTask = new UpdateCurrentWalletTask(
                multiAddressesImporter,
                addressSequentialGenerator,
                bitcoindRpcClient,
                taskBuilder
        );
        CurrentReceivingAddress currentReceivingAddressMock = mock(CurrentReceivingAddress.class);
        ObservableList<ReceivingAddress> receivingAddresses = (ObservableList<ReceivingAddress>) mock(ObservableList.class);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(new ArrayList<>());

        updateCurrentWalletTask.setInitialAddressToMonitor(20);
        updateCurrentWalletTask.setCurrentReceivingAddress(currentReceivingAddressMock)
                .setReceivingAddresses(receivingAddresses)
                .setSeed(seed)
                .update();

        verify(multiAddressesImporter).importMultiAddresses(expectedAddresses);
        verify(receivingAddresses).addAll(argThat((Collection<ReceivingAddress> args) -> args.isEmpty()));
        ArgumentCaptor<ReceivingAddress> argumentCaptor = ArgumentCaptor.forClass(ReceivingAddress.class);
        verify(currentReceivingAddressMock).setReceivingAddress(argumentCaptor.capture());
        ReceivingAddress actualArgument = argumentCaptor.getValue();
        assertTrue(
            actualArgument.getAddress().equals(expectedAddresses[0]) &&
                    actualArgument.getConfirmations() == 0 &&
                    actualArgument.getBalance().equals("0")
        );
    }

    @Test
    void updateWithFirstUsedAddresses() {
        UpdateCurrentWalletTask updateCurrentWalletTask = new UpdateCurrentWalletTask(
                multiAddressesImporter,
                addressSequentialGenerator,
                bitcoindRpcClient,
                taskBuilder
        );
        CurrentReceivingAddress currentReceivingAddressMock = mock(CurrentReceivingAddress.class);
        ObservableList<ReceivingAddress> receivingAddresses = (ObservableList<ReceivingAddress>) mock(ObservableList.class);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(List.of(unspent(expectedAddresses[0], TEN, 0)));

        updateCurrentWalletTask.setInitialAddressToMonitor(20);
        updateCurrentWalletTask.setCurrentReceivingAddress(currentReceivingAddressMock)
                .setReceivingAddresses(receivingAddresses)
                .setSeed(seed)
                .update();

        verify(multiAddressesImporter).importMultiAddresses(expectedAddresses);
        verify(receivingAddresses).addAll(
                argThat((Collection<ReceivingAddress> args) -> {
                    ReceivingAddress[] array = args.toArray(new ReceivingAddress[0]);
                    return array[0].getBalance().equals("10") &&
                        array[0].getConfirmations() == 0 &&
                        array[0].getAddress().equals(expectedAddresses[0]);
                })
        );
        ArgumentCaptor<ReceivingAddress> argumentCaptor = ArgumentCaptor.forClass(ReceivingAddress.class);
        verify(currentReceivingAddressMock).setReceivingAddress(argumentCaptor.capture());
        ReceivingAddress actualArgument = argumentCaptor.getValue();
        assertTrue(
                actualArgument.getAddress().equals(expectedAddresses[1]) &&
                        actualArgument.getConfirmations() == 0 &&
                        actualArgument.getBalance().equals("0")
        );
    }

    @Test
    void updateWithFirstFiveUsedAddresses() {
        UpdateCurrentWalletTask updateCurrentWalletTask = new UpdateCurrentWalletTask(
                multiAddressesImporter,
                addressSequentialGenerator,
                bitcoindRpcClient,
                taskBuilder
        );
        CurrentReceivingAddress currentReceivingAddressMock = mock(CurrentReceivingAddress.class);
        ObservableList<ReceivingAddress> receivingAddresses = (ObservableList<ReceivingAddress>) mock(ObservableList.class);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(List.of(
                        unspent(expectedAddresses[0], TEN, 1),
                        unspent(expectedAddresses[1], TEN, 1),
                        unspent(expectedAddresses[2], TEN, 1),
                        unspent(expectedAddresses[3], TEN, 1),
                        unspent(expectedAddresses[4], TEN, 1)
                ));

        updateCurrentWalletTask.setInitialAddressToMonitor(20);
        updateCurrentWalletTask.setCurrentReceivingAddress(currentReceivingAddressMock)
                .setReceivingAddresses(receivingAddresses)
                .setSeed(seed)
                .update();

        verify(multiAddressesImporter).importMultiAddresses(expectedAddresses);
        verify(receivingAddresses).addAll(
                argThat((Collection<ReceivingAddress> args) -> {
                    ReceivingAddress[] array = args.toArray(new ReceivingAddress[0]);
                    for (int i = 0; i < array.length; i++) {
                        if (!(array[i].getBalance().equals("10") &&
                                array[i].getConfirmations() == 1 &&
                                array[i].getAddress().equals(expectedAddresses[i]))) {
                            return false;
                        }
                    }
                    return true;
                })
        );
        ArgumentCaptor<ReceivingAddress> argumentCaptor = ArgumentCaptor.forClass(ReceivingAddress.class);
        verify(currentReceivingAddressMock).setReceivingAddress(argumentCaptor.capture());
        ReceivingAddress actualArgument = argumentCaptor.getValue();
        assertTrue(
                actualArgument.getAddress().equals(expectedAddresses[5]) &&
                        actualArgument.getConfirmations() == 0 &&
                        actualArgument.getBalance().equals("0")
        );
    }

    @Test
    void updateWithFirstFiveUsedAddressesWithThreeUtxosToOneAddress() {
        UpdateCurrentWalletTask updateCurrentWalletTask = new UpdateCurrentWalletTask(
                multiAddressesImporter,
                addressSequentialGenerator,
                bitcoindRpcClient,
                taskBuilder
        );
        CurrentReceivingAddress currentReceivingAddressMock = mock(CurrentReceivingAddress.class);
        ObservableList<ReceivingAddress> receivingAddresses = (ObservableList<ReceivingAddress>) mock(ObservableList.class);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses))
                .thenReturn(List.of(
                        unspent(expectedAddresses[0], TEN, 1),
                        unspent(expectedAddresses[1], TEN, 1),
                        unspent(expectedAddresses[2], TEN, 1),
                        unspent(expectedAddresses[3], TEN, 1),
                        unspent(expectedAddresses[4], TEN, 1),
                        unspent(expectedAddresses[4], new BigDecimal(1), 0),
                        unspent(expectedAddresses[4], new BigDecimal(2), 1)
                ));

        updateCurrentWalletTask.setInitialAddressToMonitor(20);
        updateCurrentWalletTask.setCurrentReceivingAddress(currentReceivingAddressMock)
                .setReceivingAddresses(receivingAddresses)
                .setSeed(seed)
                .update();

        verify(multiAddressesImporter).importMultiAddresses(expectedAddresses);
        verify(receivingAddresses).addAll(
                argThat((Collection<ReceivingAddress> args) -> {
                    ReceivingAddress[] array = args.toArray(new ReceivingAddress[0]);
                    for (int i = 0; i < 4; i++) {
                        if (!(array[i].getBalance().equals("10") &&
                                array[i].getConfirmations() == 1 &&
                                array[i].getAddress().equals(expectedAddresses[i]))) {
                            return false;
                        }
                    }
                    return array[4].getBalance().equals("13") &&
                            array[4].getConfirmations() == 0 &&
                            array[4].getAddress().equals(expectedAddresses[4]);
                })
        );
        ArgumentCaptor<ReceivingAddress> argumentCaptor = ArgumentCaptor.forClass(ReceivingAddress.class);
        verify(currentReceivingAddressMock).setReceivingAddress(argumentCaptor.capture());
        ReceivingAddress actualArgument = argumentCaptor.getValue();
        assertTrue(
                actualArgument.getAddress().equals(expectedAddresses[5]) &&
                        actualArgument.getConfirmations() == 0 &&
                        actualArgument.getBalance().equals("0")
        );
    }

    @Test
    void updateWithTwentyUsedAddresses() {
        UpdateCurrentWalletTask updateCurrentWalletTask = new UpdateCurrentWalletTask(
                multiAddressesImporter,
                addressSequentialGenerator,
                bitcoindRpcClient,
                taskBuilder
        );
        CurrentReceivingAddress currentReceivingAddressMock = mock(CurrentReceivingAddress.class);
        ObservableList<ReceivingAddress> receivingAddresses = (ObservableList<ReceivingAddress>) mock(ObservableList.class);
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");

        String[] expectedAddresses = expectedAddresses(seed, 20, FIRST_BIP84_ADDRESS_PATH);
        String[] expectedAddresses2 = expectedAddresses(seed, 20, new DerivationPath("84'/0'/0'/0/20"));
        List<Unspent> unspents = IntStream.range(0, 20)
                .mapToObj(i -> unspent(expectedAddresses[i], TEN, 1))
                .collect(Collectors.toList());
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses)).thenReturn(unspents);
        when(bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, expectedAddresses2)).thenReturn(new ArrayList<>());

        updateCurrentWalletTask.setInitialAddressToMonitor(20);
        updateCurrentWalletTask.setCurrentReceivingAddress(currentReceivingAddressMock)
                .setReceivingAddresses(receivingAddresses)
                .setSeed(seed)
                .update();

        ArgumentCaptor<String[]> expectedAddressesCaptured = ArgumentCaptor.forClass(String[].class);
        verify(multiAddressesImporter, times(2)).importMultiAddresses(expectedAddressesCaptured.capture());
        List<String[]> allExpectedAddresses = expectedAddressesCaptured.getAllValues();
        assertEquals(40, new HashSet<>(allExpectedAddresses).size());
        assertArrayEquals(allExpectedAddresses.subList(0, 20).toArray(), expectedAddresses);
        assertArrayEquals(allExpectedAddresses.subList(20, 40).toArray(), expectedAddresses2);

        verify(receivingAddresses, times(2)).addAll(receivingAddressesCaptured.capture());
        assertTrue(() -> {
                    ReceivingAddress[] array = receivingAddressesCaptured.getAllValues().get(0).toArray(new ReceivingAddress[0]);
                    for (int i = 0; i < array.length; i++) {
                        if (!(array[i].getBalance().equals("10") &&
                                array[i].getConfirmations() == 1 &&
                                array[i].getAddress().equals(expectedAddresses[i]))) {
                            return false;
                        }
                    }
                    return true;
                }
        );
        assertTrue(() -> receivingAddressesCaptured.getAllValues().get(1).isEmpty());

        ArgumentCaptor<ReceivingAddress> argumentCaptor = ArgumentCaptor.forClass(ReceivingAddress.class);
        verify(currentReceivingAddressMock).setReceivingAddress(argumentCaptor.capture());
        ReceivingAddress actualArgument = argumentCaptor.getValue();
        String expectedAddress = expectedAddresses(seed, 1, new DerivationPath("84'/0'/0'/0/20"))[0];

        assertTrue(
            actualArgument.getAddress().equals(expectedAddress) &&
                    actualArgument.getConfirmations() == 0 &&
                    actualArgument.getBalance().equals("0"),
            format("actual address: %s, expected address: %s", actualArgument.getAddress(), expectedAddress)
                .concat(
                    format("\nactual confirmations: %s, expected confirmations: %s",
                            actualArgument.getConfirmations(),
                            0
                    )
                ).concat(
                        format("\nactual balance: %s, expected balance: %s",
                                actualArgument.getBalance(),
                                "0"
                        )
                )
        );
    }

    private Unspent unspent(String expectedAddress, BigDecimal amount, int confirmations) {
        return new Unspent() {
            @Override
            public String account() {
                return "test";
            }

            @Override
            public int confirmations() {
                return confirmations;
            }

            @Override
            public String txid() {
                return null;
            }

            @Override
            public Integer vout() {
                return null;
            }

            @Override
            public String scriptPubKey() {
                return null;
            }

            @Override
            public String address() {
                return expectedAddress;
            }

            @Override
            public BigDecimal amount() {
                return amount;
            }

            @Override
            public byte[] data() {
                return new byte[0];
            }
        };
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