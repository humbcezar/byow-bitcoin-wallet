package byow.bitcoinwallet.services.wallet;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.entities.XPub;
import byow.bitcoinwallet.repositories.XPubRepository;
import byow.bitcoinwallet.services.address.XPubKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XPubCreator {
    private final XPubRepository xPubRepository;

    private final List<XPubKeyGenerator> xPubKeyGenerators;

    @Autowired
    public XPubCreator(XPubRepository xPubRepository, List<XPubKeyGenerator> xPubKeyGenerators) {
        this.xPubRepository = xPubRepository;
        this.xPubKeyGenerators = xPubKeyGenerators;
    }

    public void create(String seed, Wallet wallet) {
        xPubKeyGenerators.forEach(xPubKeyGenerator -> {
            xPubRepository.save(
                new XPub(
                    xPubKeyGenerator.generateXPubkeySerialized(seed),
                    xPubKeyGenerator.getType().toString(),
                    wallet
                )
            );
        });
    }
}
