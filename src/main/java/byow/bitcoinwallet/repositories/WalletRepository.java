package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface WalletRepository extends CrudRepository<Wallet, Long> {
    Wallet findByName(String name);
}
