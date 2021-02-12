package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.TransactionOutput;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionOutputRepository extends JpaRepository<TransactionOutput, Long> {
}
