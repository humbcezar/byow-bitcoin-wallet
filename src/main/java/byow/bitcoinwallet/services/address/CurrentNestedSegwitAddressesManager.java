package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.NextNestedSegwitAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.UtxosGetter;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.transaction.TransactionSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP49_ADDRESS_PATH;
import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class CurrentNestedSegwitAddressesManager extends CurrentAddressesManager {

    @Autowired
    public CurrentNestedSegwitAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        @Qualifier("nestedSegwitAddressSequentialGenerator") AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        NextNestedSegwitAddress nextNestedSegwitAddress,
        TransactionSaver transactionSaver,
        TransactionOutputRepository transactionOutputRepository,
        WalletRepository walletRepository,
        AddressRepository addressRepository
    ) {
        super(
            currentReceivingAddresses,
            addressSequentialGenerator,
            multiAddressesImporter,
            currentReceivingAddressesUpdater,
            utxosGetter,
            transactionSaver,
            transactionOutputRepository,
            walletRepository,
            addressRepository
        );
        this.nextAddress = nextNestedSegwitAddress;
        this.currentDerivationPath = FIRST_BIP49_ADDRESS_PATH;
    }

    @Override
    public void clear() {
        currentDerivationPath = FIRST_BIP49_ADDRESS_PATH;
        nextAddress.setReceivingAddress(
                new ReceivingAddress(ZERO, 0, "")
        );
    }
}
