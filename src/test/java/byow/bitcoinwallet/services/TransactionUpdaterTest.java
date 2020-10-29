package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.RawTransaction.Out.ScriptPubKey;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionUpdaterTest {

    @Mock
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    @Mock
    private CurrentWalletManager currentWalletManager;

    @Mock
    private RawTransaction rawTransaction;

    @Mock
    private Out out;

    @Mock
    private ScriptPubKey scriptPubKey;

    @Test
    public void updateOneTransaction() {
        List<String> addresses = List.of("addresstest");
        updateNTransactions(addresses);
    }

    @Test
    public void updateEightTransactions() {
        List<String> addresses = List.of("addresstest", "addresstest2");
        updateNTransactions(addresses);
    }

    private void updateNTransactions(List<String> addresses) {
        TransactionUpdater transactionUpdater = new TransactionUpdater(currentReceivingAddressesManager, currentWalletManager);

        when(scriptPubKey.addresses()).thenReturn(addresses);
        when(out.scriptPubKey()).thenReturn(scriptPubKey);
        when(rawTransaction.vOut()).thenReturn(List.of(out));
        addresses.forEach(address -> when(currentReceivingAddressesManager.contains(address)).thenReturn(true));
        String seed = "testseed";
        Wallet wallet = new Wallet("testname", seed);
        wallet.setCreatedAt(new Date());
        when(currentWalletManager.getCurrentWallet()).thenReturn(wallet);
        addresses.forEach(address ->
            when(currentReceivingAddressesManager.updateReceivingAddresses(List.of(address), wallet.getCreatedAt())).thenReturn(1)
        );

        transactionUpdater.update(rawTransaction);

        verify(currentReceivingAddressesManager).updateNextAddress("", addresses.size(), seed);
    }
}