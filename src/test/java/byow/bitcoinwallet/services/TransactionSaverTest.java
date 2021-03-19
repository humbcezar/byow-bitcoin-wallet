package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.*;
import byow.bitcoinwallet.repositories.*;
import byow.bitcoinwallet.services.transaction.TransactionSaver;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static com.blockstream.libwally.Wally.*;
import static org.junit.jupiter.api.Assertions.*;
import static wf.bitcoin.krotjson.HexCoder.encode;

@SpringBootTest
@ActiveProfiles("test")
public class TransactionSaverTest {
    @Autowired
    private TransactionSaver transactionSaver;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionInputRepository transactionInputRepository;

    @Autowired
    private TransactionOutputRepository transactionOutputRepository;

    @Autowired
    private AddressRepository addressRepository;

    private String txHex = "020000000001012f94ddd965758445be2dfac132c5e75c517edf5ea04b745a953d0bc04c32829901000000006aedc98002a8c500000000000022002009246bbe3beb48cf1f6f2954f90d648eb04d68570b797e104fead9e6c3c87fd40544020000000000160014c221cdfc1b867d82f19d761d4e09f3b6216d8a8304004830450221008aaa56e4f0efa1f7b7ed690944ac1b59f046a59306fcd1d09924936bd500046d02202b22e13a2ad7e16a0390d726c56dfc9f07647f7abcfac651e35e5dc9d830fc8a01483045022100e096ad0acdc9e8261d1cdad973f7f234ee84a6ee68e0b89ff0c1370896e63fe102202ec36d7554d1feac8bc297279f89830da98953664b73d38767e81ee0763b9988014752210390134e68561872313ba59e56700732483f4a43c2de24559cb8c7039f25f7faf821039eb59b267a78f1020f27a83dc5e3b1e4157e4a517774040a196e9f43f08ad17d52ae89a3b720";

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        transactionInputRepository.deleteAll();
        transactionOutputRepository.deleteAll();
    }

    @Test
    public void save() {
        Wallet wallet = new Wallet(RandomString.make(), RandomString.make());
        walletRepository.save(wallet);

        Object transaction = tx_from_hex(txHex, WALLY_TX_FLAG_USE_WITNESS);

        Address address1 = addressRepository.save(new Address(RandomString.make()));
        Address address2 = addressRepository.save(new Address(RandomString.make()));
        TransactionInput transactionInput = new TransactionInput(address1, 1L);
        transactionInputRepository.save(transactionInput);
        TransactionOutput transactionOutput = new TransactionOutput(address2, 1L);
        transactionOutputRepository.save(transactionOutput);

        Transaction savedTransaction = transactionSaver.save(
            encode(tx_get_txid(transaction)),
            wallet,
            Set.of(transactionInput),
            Set.of(transactionOutput)
        );
        Transaction readTransaction = transactionRepository.findById(savedTransaction.getId()).orElseThrow();
        assertEquals(savedTransaction.getId(), readTransaction.getId());
        assertEquals(savedTransaction.getTxId(), readTransaction.getTxId());
        assertEquals(savedTransaction.getCreatedAt(), readTransaction.getCreatedAt());
        assertTrue(readTransaction.getWallets().contains(wallet));
        assertTrue(readTransaction.getTransactionInputs().contains(transactionInput));
        assertTrue(readTransaction.getTransactionOutputs().contains(transactionOutput));
        Wallet savedWallet = walletRepository.findById(wallet.getId()).orElseThrow();
        assertEquals(savedWallet.getTransactions().get(0).getId(), savedTransaction.getId());
    }
}