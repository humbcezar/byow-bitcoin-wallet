package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.entities.XPub;
import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.services.address.XPubKeyGenerator;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.address.DefaultAddressGeneratorBySeed;
import byow.bitcoinwallet.services.address.SeedGenerator;
import byow.bitcoinwallet.services.transaction.SingleRandomDrawCoinSelector;
import byow.bitcoinwallet.utils.UnspentUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class SingleRandomDrawCoinSelectorTest {
    @Autowired
    private SingleRandomDrawCoinSelector singleRandomDrawTransactionCreator;

    @Autowired
    private UnspentUtil unspentUtil;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private DefaultAddressGeneratorBySeed addressGenerator;

    @Autowired
    private List<XPubKeyGenerator> xPubKeyGenerators;

    @MockBean
    private CurrentReceivingAddresses currentReceivingAddresses;

    @Test
    public void createTransactionWithOneInputWithChange() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(100);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress,
            FIRST_BIP84_ADDRESS_PATH
        );
        when(currentReceivingAddresses.getReceivingAddress(inputAddress)).thenReturn(receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
            List.of(utxo),
            ONE,
            new BigDecimal("0.002"),
            xPubs,
            outputAddress,
            changeAddress
        );
        assertEquals(1, transaction.getInputCount());
        assertEquals(2, transaction.getOutputCount());
        assertEquals(100000000, transaction.getOutput(0).getAmount());
        assertEquals(9899973210L, transaction.getOutput(1).getAmount());
        assertEquals(utxo.txid(), transaction.getInput(0).getTxId());
        assertEquals(26790, transaction.getIntendedTotalFeeInSatoshis());
    }

    @Test
    public void createTransactionWithTwoInputsWithChange() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress1 = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String inputAddress2 = "bcrt1qxmv2gr88cs8m5gckaeccr445arpvuqv79mwx5q";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(100);

        ReceivingAddress receivingInputAddress1 = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress1,
            FIRST_BIP84_ADDRESS_PATH
        );

        ReceivingAddress receivingInputAddress2 = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress2,
            FIRST_BIP84_ADDRESS_PATH.next(1)
        );

        when(currentReceivingAddresses.getReceivingAddress(inputAddress1)).thenReturn(receivingInputAddress1);
        when(currentReceivingAddresses.getReceivingAddress(inputAddress2)).thenReturn(receivingInputAddress2);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo1 = unspentUtil.unspent(inputAddress1, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        Unspent utxo2 = unspentUtil.unspent(inputAddress2, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
            List.of(utxo1, utxo2),
            new BigDecimal(150),
            new BigDecimal("0.002"),
            xPubs,
            outputAddress,
            changeAddress
        );
        assertEquals(2, transaction.getInputCount());
        assertEquals(2, transaction.getOutputCount());
        assertEquals(15000000000L, transaction.getOutput(0).getAmount());
        assertEquals(4999960290L, transaction.getOutput(1).getAmount());
        assertEquals(39710, transaction.getIntendedTotalFeeInSatoshis());
    }

    @Test
    public void createTransactionWithOneInsufficientInputReturnNull() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(100);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
                inputBalance,
                1,
                inputAddress,
                FIRST_BIP84_ADDRESS_PATH
        );
        when(currentReceivingAddresses.getReceivingAddress(inputAddress)).thenReturn(receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
                List.of(utxo),
                valueOf(110),
                new BigDecimal("0.002"),
                xPubs,
                outputAddress,
                changeAddress
        );
        assertNull(transaction);
    }

    @Test
    public void createTransactionWithOneInsufficientInputEqualToTarget() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(110);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress,
            FIRST_BIP84_ADDRESS_PATH
        );
        when(currentReceivingAddresses.getReceivingAddress(inputAddress)).thenReturn(receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
            List.of(utxo),
            valueOf(110),
            new BigDecimal("0.002"),
            xPubs,
            outputAddress,
            changeAddress
        );
        assertEquals(1, transaction.getInputCount());
        assertEquals(1, transaction.getOutputCount());
        assertEquals(11000000000L, transaction.getOutput(0).getAmount());
        assertEquals(20900, transaction.getIntendedTotalFeeInSatoshis());
    }

    @Test
    public void createTransactionWithTotalFeeGreaterThanDustButLesserThanIntendedFee() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(1.00020000);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress,
            FIRST_BIP84_ADDRESS_PATH
        );
        when(currentReceivingAddresses.getReceivingAddress(inputAddress)).thenReturn(receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
            List.of(utxo),
            valueOf(1),
            new BigDecimal("0.002"),
            xPubs,
            outputAddress,
            changeAddress
        );
        assertEquals(1, transaction.getInputCount());
        assertEquals(1, transaction.getOutputCount());
        assertEquals(100_000_000, transaction.getOutput(0).getAmount());
        assertEquals(20000, transaction.getTotalFeeInSatoshis());
        assertEquals(20900, transaction.getIntendedTotalFeeInSatoshis());
    }

    @Test
    public void createTransactionWithDustFee() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(1);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress,
            FIRST_BIP84_ADDRESS_PATH
        );
        when(currentReceivingAddresses.getReceivingAddress(inputAddress)).thenReturn(receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 922, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
                List.of(utxo),
                valueOf(0.99999800),
                new BigDecimal("0.002"),
                xPubs,
                outputAddress,
                changeAddress
        );
        assertEquals(1, transaction.getInputCount());
        assertEquals(1, transaction.getOutputCount());
        assertEquals(99999800, transaction.getOutput(0).getAmount());
        assertEquals(200, transaction.getTotalFeeInSatoshis());
        assertEquals(20900, transaction.getIntendedTotalFeeInSatoshis());
    }

    @Test
    public void createTransactionWithOneInputWithZeroConfirmationReturnNull() {
        String seed = seedGenerator.generateSeedAsString(seedGenerator.generateMnemonicSeed(), "");
        Set<XPub> xPubs = getXPubs(seed);
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(100);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
            inputBalance,
            0,
            inputAddress,
            FIRST_BIP84_ADDRESS_PATH
        );
        when(currentReceivingAddresses.getReceivingAddress(inputAddress)).thenReturn(receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 0, "58791da7eb6b282c15bc4fc38d3b8ff903f7277a2d4bdf50ff6fbf10b1d8c0c6");
        WallyTransaction transaction = singleRandomDrawTransactionCreator.select(
            List.of(utxo),
            ONE,
            new BigDecimal("0.002"),
            xPubs,
            outputAddress,
            changeAddress
        );
        assertNull(transaction);
    }
    //TODO: limpar transaction, inputs e outputs apos usa-las

    private Set<XPub> getXPubs(String seed) {
        Wallet wallet = new Wallet();
        return xPubKeyGenerators.stream()
            .map(xPubKeyGenerator -> new XPub(xPubKeyGenerator.generateXPubkeySerialized(seed), xPubKeyGenerator.getType().toString(), wallet))
            .collect(Collectors.toSet());
    }

}