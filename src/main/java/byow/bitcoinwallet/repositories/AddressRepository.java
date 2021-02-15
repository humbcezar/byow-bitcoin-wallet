package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByAddress(String address);

    boolean existsByAddress(String address);
}
