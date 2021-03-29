package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.XPubTypes;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.UtxosGetter;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.transaction.TransactionSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.entities.XPubTypes.DEFAULT_X_PUB;
import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP84_ADDRESS_PATH;
import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class CurrentDefaultAddressesManager extends CurrentAddressesManager {
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
        WalletRepository walletRepository,
        AddressRepository addressRepository,
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
        this.nextAddress = nextReceivingAddress;
        this.currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;
    }

    @Override
    public void clear() {
        currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;
        nextAddress.setReceivingAddress(
            new ReceivingAddress(ZERO, 0, "")
        );
    }

    @Override
    public XPubTypes getXPubType() {
        return DEFAULT_X_PUB;
    }
}
