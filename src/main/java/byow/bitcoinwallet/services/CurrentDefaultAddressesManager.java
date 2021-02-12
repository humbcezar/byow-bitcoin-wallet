package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.NextAddress;
import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class CurrentDefaultAddressesManager extends CurrentAddressesManager {
    private final NextReceivingAddress nextReceivingAddress;

    protected DerivationPath currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;

    @Autowired
    public CurrentDefaultAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        AddressSequentialGenerator addressSequentialGenerator,
        NextReceivingAddress nextReceivingAddress,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        TransactionSaver transactionSaver,
        TransactionOutputRepository transactionOutputRepository,
        WalletRepository walletRepository
    ) {
        super(
            currentReceivingAddresses,
            addressSequentialGenerator,
            multiAddressesImporter,
            currentReceivingAddressesUpdater,
            utxosGetter,
            transactionSaver,
            transactionOutputRepository,
            walletRepository
        );
        this.nextReceivingAddress = nextReceivingAddress;
    }

    public void clear() {
        currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;
        nextReceivingAddress.setReceivingAddress(
            new ReceivingAddress(ZERO, 0, "")
        );
    }

    @Override
    protected void setNextAddress(ReceivingAddress address) {
        nextReceivingAddress.setReceivingAddress(address);
    }

    @Override
    protected NextAddress getNextAddress() {
        return nextReceivingAddress;
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
}
