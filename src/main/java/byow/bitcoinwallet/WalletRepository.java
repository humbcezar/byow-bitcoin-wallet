package byow.bitcoinwallet;

import org.springframework.data.repository.CrudRepository;

public interface WalletRepository extends CrudRepository<Wallet, Long> {
    Wallet findByName(String name);
}
