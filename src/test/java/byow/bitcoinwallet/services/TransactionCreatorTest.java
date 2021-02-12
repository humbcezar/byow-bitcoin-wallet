package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.WallyTransaction;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.utils.UnspentUtil;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.math.BigDecimal.ZERO;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
public class TransactionCreatorTest {

    @Autowired
    private UnspentUtil unspentUtil;

    @Autowired
    private TransactionCreator transactionCreator;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private DefaultKeyGenerator defaultKeyGenerator;

    @MockBean
    private FeeEstimator feeEstimator;

    @MockBean
    private CoinSelector coinSelector;

    @MockBean
    private UtxosGetter utxosGetter;

    @MockBean
    private CurrentWalletManager currentWalletManager;

    @MockBean
    private NextChangeAddress nextChangeAddress;

    @MockBean
    private CurrentReceivingAddresses currentReceivingAddresses;

    @Test
    public void createOneTransaction() {
        String seed = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        String addressToSend = addressGenerator.generate(seed, FIRST_BIP84_ADDRESS_PATH);

        String seed2 = seedGenerator.generateSeed(seedGenerator.generateMnemonicSeed(), "");
        String unspentAddress = addressGenerator.generate(seed2, FIRST_BIP84_ADDRESS_PATH);
        String changeAddress = addressGenerator.generate(seed2, FIRST_BIP84_ADDRESS_PATH.next(2));

        Unspent utxo = unspentUtil.unspent(unspentAddress, BigDecimal.ONE, 1, RandomString.make());
        List<Unspent> unspents = List.of(utxo);
        when(utxosGetter.getUtxos()).thenReturn(unspents);
        ReceivingAddress receivingAddress = new ReceivingAddress(utxo.amount(), utxo.confirmations(), utxo.address());
        when(currentReceivingAddresses.getReceivingAddress(unspentAddress)).thenReturn(receivingAddress);
        Wallet wallet = new Wallet("test", seed2);
        when(currentWalletManager.getCurrentWallet()).thenReturn(wallet);
        ReceivingAddress changeReceivingAddress = new ReceivingAddress(ZERO, 0, changeAddress);
        when(nextChangeAddress.getValue()).thenReturn(changeReceivingAddress);
        when(feeEstimator.estimate()).thenReturn(new BigDecimal("0.0002"));
        WallyTransaction transaction = mock(WallyTransaction.class);
        when(transaction.getInputCount()).thenReturn(1);
        when(
            coinSelector.select(
                unspents,
                new BigDecimal("0.5"),
                new BigDecimal("0.0002"),
                seed2,
                addressToSend,
                changeAddress
            )
        ).thenReturn(transaction);

        transactionCreator.create(addressToSend, new BigDecimal("0.5"));

        verify(feeEstimator).estimate();
        verify(coinSelector).select(
            unspents,
            new BigDecimal("0.5"),
            new BigDecimal("0.0002"),
            seed2,
            addressToSend,
            changeAddress
        );
    }
}