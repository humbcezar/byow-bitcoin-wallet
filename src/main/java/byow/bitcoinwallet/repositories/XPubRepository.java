package byow.bitcoinwallet.repositories;

import byow.bitcoinwallet.entities.XPub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XPubRepository extends JpaRepository<XPub, Long> {
}
