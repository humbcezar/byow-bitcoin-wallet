package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Date;
import java.util.List;

import static com.blockstream.libwally.Wally.WALLY_TX_FLAG_USE_WITNESS;
import static com.blockstream.libwally.Wally.tx_from_hex;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionUpdaterTest {

    @Mock
    private CurrentAddressesManager currentAddressesManager;

    @Mock
    private CurrentWalletManager currentWalletManager;

    @Mock
    private NextReceivingAddress nextReceivingAddress;

    @Mock
    private NextChangeAddress nextChangeAddress;

    private String txHex = "020000000001012f94ddd965758445be2dfac132c5e75c517edf5ea04b745a953d0bc04c32829901000000006aedc98002a8c500000000000022002009246bbe3beb48cf1f6f2954f90d648eb04d68570b797e104fead9e6c3c87fd40544020000000000160014c221cdfc1b867d82f19d761d4e09f3b6216d8a8304004830450221008aaa56e4f0efa1f7b7ed690944ac1b59f046a59306fcd1d09924936bd500046d02202b22e13a2ad7e16a0390d726c56dfc9f07647f7abcfac651e35e5dc9d830fc8a01483045022100e096ad0acdc9e8261d1cdad973f7f234ee84a6ee68e0b89ff0c1370896e63fe102202ec36d7554d1feac8bc297279f89830da98953664b73d38767e81ee0763b9988014752210390134e68561872313ba59e56700732483f4a43c2de24559cb8c7039f25f7faf821039eb59b267a78f1020f27a83dc5e3b1e4157e4a517774040a196e9f43f08ad17d52ae89a3b720";

    @Test
    public void updateOneTransaction() {
        List<String> addresses = List.of("bcrt1qpyjxh03madyv78m09920jrty36cy66zhpduhuyz0atv7ds7g0l2q5f8rf8");
        updateNTransactions(addresses);
    }

    @Test
    public void updateEightTransactions() {
        List<String> addresses = List.of("bcrt1qpyjxh03madyv78m09920jrty36cy66zhpduhuyz0atv7ds7g0l2q5f8rf8");
        updateNTransactions(addresses);
    }

    private void updateNTransactions(List<String> addresses) {
        TransactionUpdater transactionUpdater = new TransactionUpdater(
            currentAddressesManager,
            currentWalletManager,
            nextReceivingAddress,
            nextChangeAddress,
            "bcrt"
        );
        Object transaction = tx_from_hex(txHex, WALLY_TX_FLAG_USE_WITNESS);

        addresses.forEach(address -> when(currentAddressesManager.contains(address)).thenReturn(true));
        String seed = "testseed";
        Wallet wallet = new Wallet("testname", seed);
        Date date = new Date();
        wallet.setCreatedAt(date);
        when(currentWalletManager.getCurrentWallet()).thenReturn(wallet);
        when(nextReceivingAddress.equalAddress(any())).thenReturn(true);
        addresses.forEach(address ->
            when(currentAddressesManager.updateReceivingAddresses(List.of(address))).thenReturn(1)
        );

        transactionUpdater.update(transaction);

        verify(currentAddressesManager, times(addresses.size())).updateNextReceivingAddress("", 1, seed, date);
    }
}