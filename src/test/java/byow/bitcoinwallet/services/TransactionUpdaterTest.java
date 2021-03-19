package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.services.address.*;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.gui.CurrentTransactions;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.transaction.TransactionSaver;
import byow.bitcoinwallet.services.transaction.TransactionUpdater;
import byow.bitcoinwallet.utils.UnspentUtil;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.util.Date;
import java.util.List;

import static com.blockstream.libwally.Wally.*;
import static java.math.BigDecimal.ZERO;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class TransactionUpdaterTest {

    private List<CurrentAddressesManager> currentAddressesManagers;

    private UnspentUtil unspentUtil = new UnspentUtil();

    @Mock
    private CurrentDefaultAddressesManager currentDefaultAddressesManager;

    @Mock
    private CurrentDefaultChangeAddressesManager currentDefaultChangeAddressesManager;

    @Mock
    private CurrentReceivingAddresses currentReceivingAddresses;

    @Mock
    private NextReceivingAddress nextReceivingAddress;

    @Mock
    private CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater;

    @Mock
    private TransactionSaver transactionSaver;

    @Mock
    private UtxosGetter utxosGetter;

    @Mock
    private TransactionOutputRepository transactionOutputRepository;

    @Mock
    private CurrentTransactions currentTransactions;

    @Mock
    private CurrentWallet currentWallet;

    @Mock
    private AddressUpdater addressUpdater;

    @Mock
    private InputAddressesParser inputAddressesParser;

    @Mock
    private OutputAddressesParser outputAddressesParser;

    @Mock
    private AddressRepository addressRepository;

    private String txHex = "020000000001012f94ddd965758445be2dfac132c5e75c517edf5ea04b745a953d0bc04c32829901000000006aedc98002a8c500000000000022002009246bbe3beb48cf1f6f2954f90d648eb04d68570b797e104fead9e6c3c87fd40544020000000000160014c221cdfc1b867d82f19d761d4e09f3b6216d8a8304004830450221008aaa56e4f0efa1f7b7ed690944ac1b59f046a59306fcd1d09924936bd500046d02202b22e13a2ad7e16a0390d726c56dfc9f07647f7abcfac651e35e5dc9d830fc8a01483045022100e096ad0acdc9e8261d1cdad973f7f234ee84a6ee68e0b89ff0c1370896e63fe102202ec36d7554d1feac8bc297279f89830da98953664b73d38767e81ee0763b9988014752210390134e68561872313ba59e56700732483f4a43c2de24559cb8c7039f25f7faf821039eb59b267a78f1020f27a83dc5e3b1e4157e4a517774040a196e9f43f08ad17d52ae89a3b720";

    @BeforeEach
    void setUp() {
        currentAddressesManagers = List.of(currentDefaultAddressesManager, currentDefaultChangeAddressesManager);
    }

    @Test
    public void updateOneTransaction() {
        List<String> addresses = List.of("bcrt1qpyjxh03madyv78m09920jrty36cy66zhpduhuyz0atv7ds7g0l2q5f8rf8");
        updateNTransactions(addresses);
    }

    private void updateNTransactions(List<String> addresses) {
        TransactionUpdater transactionUpdater = new TransactionUpdater(
            currentReceivingAddresses,
            currentWallet,
            transactionSaver,
            transactionOutputRepository,
            currentTransactions,
            addressUpdater,
            inputAddressesParser,
            outputAddressesParser,
            addressRepository
        );
        Object transaction = tx_from_hex(txHex, WALLY_TX_FLAG_USE_WITNESS);

        addresses.forEach(address -> when(currentReceivingAddresses.contains(address)).thenReturn(true));
        String seed = "testseed";
        Wallet wallet = new Wallet("testname", seed);
        Date date = new Date();
        wallet.setCreatedAt(date);
        when(currentWallet.getCurrentWallet()).thenReturn(wallet);
        List<Unspent> utxosList = List.of(unspentUtil.unspent("", ZERO, 0, RandomString.make()));
        when(utxosGetter.getUtxos(any())).thenReturn(utxosList);
        addresses.forEach(address ->
            when(currentReceivingAddressesUpdater.updateReceivingAddresses(
                    List.of(address),
                    utxosList
                )
            ).thenReturn(1)
        );

        when(nextReceivingAddress.equalAddress(any())).thenReturn(true);
        when(currentDefaultAddressesManager.getNextAddress()).thenReturn(nextReceivingAddress);
        when(currentDefaultChangeAddressesManager.getNextAddress()).thenReturn(nextReceivingAddress);

        transactionUpdater.update(transaction);

        verify(currentDefaultAddressesManager, times(addresses.size())).updateNextAddress("", 1, seed, date, "");
//        verify(transactionSaver).save(encode(tx_get_txid(transaction)), currentWalletManager.getCurrentWallet());
    }
}