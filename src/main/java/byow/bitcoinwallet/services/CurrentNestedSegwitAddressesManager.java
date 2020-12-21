package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextAddress;
import byow.bitcoinwallet.entities.NextNestedSegwitAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP49_ADDRESS_PATH;
import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class CurrentNestedSegwitAddressesManager extends CurrentAddressesManager {
    private final NextNestedSegwitAddress nextNestedSegwitAddress;

    protected DerivationPath currentDerivationPath = FIRST_BIP49_ADDRESS_PATH;

    @Autowired
    public CurrentNestedSegwitAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        @Qualifier("nestedSegwitAddressSequentialGenerator") AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        NextNestedSegwitAddress nextNestedSegwitAddress
    ) {
        super(
            currentReceivingAddresses,
            addressSequentialGenerator,
            multiAddressesImporter,
            currentReceivingAddressesUpdater,
            utxosGetter
        );
        this.nextNestedSegwitAddress = nextNestedSegwitAddress;
    }

    @Override
    protected void setNextAddress(ReceivingAddress address) {
        nextNestedSegwitAddress.setReceivingAddress(address);
    }

    @Override
    protected NextAddress getNextAddress() {
        return nextNestedSegwitAddress;
    }

    @Override
    protected DerivationPath getCurrentDerivationPath() {
        return currentDerivationPath;
    }

    @Override
    protected DerivationPath setNextCurrentDerivationPath(int next) {
        currentDerivationPath = currentDerivationPath.next(next);
        return currentDerivationPath;
    }

    @Override
    public void clear() {
        currentDerivationPath = FIRST_BIP49_ADDRESS_PATH;
        nextNestedSegwitAddress.setReceivingAddress(
                new ReceivingAddress(ZERO, 0, "")
        );
    }
}
