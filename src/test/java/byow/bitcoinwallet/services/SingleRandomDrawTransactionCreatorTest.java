package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Transaction;
import byow.bitcoinwallet.utils.UnspentUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class SingleRandomDrawTransactionCreatorTest {
    @Autowired
    private SingleRandomDrawTransactionCreator singleRandomDrawTransactionCreator;

    @Autowired
    private UnspentUtil unspentUtil;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private DefaultAddressGenerator addressGenerator;

    @Test
    public void createTransactionWithOneInputWithChange() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        String inputAddress = "bcrt1qp6lszgmk559zg6m9st08f85mc39aghwe8qlqd6";
        String changeAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(3));
        BigDecimal inputBalance = valueOf(100);
        ReceivingAddress receivingInputAddress = new ReceivingAddress(
            inputBalance,
            1,
            inputAddress,
            FIRST_BIP84_ADDRESS_PATH
        );
        Map<String, ReceivingAddress> receivingAddressMap = Map.of(inputAddress, receivingInputAddress);

        String outputAddress = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH.next(1));

        Unspent utxo = unspentUtil.unspent(inputAddress, inputBalance, 922);
        Transaction transaction = singleRandomDrawTransactionCreator.create(
            List.of(utxo),
            ONE,
            new BigDecimal("0.002"),
            receivingAddressMap,
            seed,
            outputAddress,
            changeAddress
        );
        assertEquals(1, transaction.getInputCount());
        assertEquals(2, transaction.getOutputCount());
        assertEquals(100000000, transaction.getOutput(0).getAmount());
        assertEquals(9899973210L, transaction.getOutput(1).getAmount());
        assertEquals(utxo.txid(), transaction.getInput(0).getTxId());
    }
    //TODO: testar com multiplos utxos e ver se change muda
    //TODO: testar com dust
    //TODO: testar com inputs insuficientes
    //TODO: testar com edge cases (ex: inputs == target, inputs == adjustedTarget)
    //TODO: limpar transaction, inputs e outputs apos usa-las
}