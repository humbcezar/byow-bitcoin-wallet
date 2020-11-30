package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Transaction;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.utils.UnspentUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.math.BigDecimal.ZERO;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
public class SendTransactionServiceTest {
    @Autowired
    private UnspentUtil unspentUtil;

    @Autowired
    private SendTransactionService sendTransactionService;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private DefaultKeyGenerator defaultKeyGenerator;

    @MockBean
    private FeeEstimator feeEstimator;

    @MockBean
    private TransactionCreator transactionCreator;

    @MockBean
    private TransactionSender transactionSender;

    @MockBean
    private CurrentAddressesManager currentAddressesManager;

    @MockBean
    private CurrentWalletManager currentWalletManager;

    @MockBean
    private NextChangeAddress nextChangeAddress;

    @Test
    public void sendOneTransaction() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        String addressToSend = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);

        String seed2 = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        String unspentAddress = addressGenerator.generate(seed2, FIRST_BIP84_ADDRESS_PATH);
        String changeAddress = addressGenerator.generate(seed2, FIRST_BIP84_ADDRESS_PATH.next(2));

        Unspent utxo = unspentUtil.unspent(unspentAddress, BigDecimal.ONE, 1);
        List<Unspent> unspents = List.of(utxo);
        when(currentAddressesManager.getUtxos()).thenReturn(unspents);
        ReceivingAddress receivingAddress = new ReceivingAddress(utxo.amount(), utxo.confirmations(), utxo.address());
        Map<String, ReceivingAddress> receivingAddressMap = Map.of(unspentAddress, receivingAddress);
        when(currentAddressesManager.getReceivingAddressesMap()).thenReturn(receivingAddressMap);
        Wallet wallet = new Wallet("test", seed2);
        when(currentWalletManager.getCurrentWallet()).thenReturn(wallet);
        ReceivingAddress changeReceivingAddress = new ReceivingAddress(ZERO, 0, changeAddress);
        when(nextChangeAddress.getReceivingAddress()).thenReturn(changeReceivingAddress);
        when(feeEstimator.estimate()).thenReturn(new BigDecimal("0.0002"));
        Transaction transaction = mock(Transaction.class);
        when(transaction.getInputCount()).thenReturn(1);
        when(
            transactionCreator.create(
                unspents,
                new BigDecimal("0.5"),
                new BigDecimal("0.0002"),
                receivingAddressMap,
                seed2,
                addressToSend,
                changeAddress
            )
        ).thenReturn(transaction);

        sendTransactionService.send(addressToSend, new BigDecimal("0.5"));

        verify(feeEstimator).estimate();
        verify(transaction).sign(0);
        verify(transactionCreator).create(
                unspents,
                new BigDecimal("0.5"),
                new BigDecimal("0.0002"),
                receivingAddressMap,
                seed2,
                addressToSend,
                changeAddress
        );
        verify(transactionSender).send(transaction);
    }
    //TODO: ver se change muda apos transaction

}