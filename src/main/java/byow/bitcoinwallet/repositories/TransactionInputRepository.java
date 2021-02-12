package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.TransactionInput;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionInputRepository extends JpaRepository<TransactionInput, Long> {
}
