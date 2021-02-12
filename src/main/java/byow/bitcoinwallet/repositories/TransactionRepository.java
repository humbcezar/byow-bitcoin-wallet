package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTxId(String txId);

    Optional<Transaction> findByTxIdAndWallets_Id(String txId, long id);
}
