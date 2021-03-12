package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.NextNestedSegwitChangeAddress;
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

import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP49_CHANGE_PATH;
import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class CurrentNestedSegwitChangeAddressesManager extends CurrentAddressesManager {
    @Autowired
    public CurrentNestedSegwitChangeAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        @Qualifier("nestedSegwitAddressSequentialGenerator") AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        TransactionSaver transactionSaver,
        TransactionOutputRepository transactionOutputRepository,
        WalletRepository walletRepository,
        AddressRepository addressRepository,
        NextNestedSegwitChangeAddress nextNestedSegwitChangeAddress,
        AddressesGetter addressesGetter
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
            addressRepository,
            addressesGetter
        );
        this.nextAddress = nextNestedSegwitChangeAddress;
        this.currentDerivationPath = FIRST_BIP49_CHANGE_PATH;
    }

    @Override
    public void clear() {
        currentDerivationPath = FIRST_BIP49_CHANGE_PATH;
        nextAddress.setReceivingAddress(
            new ReceivingAddress(ZERO, 0, "")
        );
    }
}