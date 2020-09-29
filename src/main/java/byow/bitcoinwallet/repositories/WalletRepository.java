package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByName(String name);
}
