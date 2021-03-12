package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.NextChangeAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.repositories.AddressRepository;
import byow.bitcoinwallet.repositories.TransactionOutputRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.services.UtxosGetter;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import byow.bitcoinwallet.services.transaction.TransactionSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static byow.bitcoinwallet.services.address.DerivationPath.FIRST_BIP84_CHANGE_PATH;
import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class CurrentDefaultChangeAddressesManager extends CurrentAddressesManager {
    @Autowired
    public CurrentDefaultChangeAddressesManager(
        CurrentReceivingAddresses currentReceivingAddresses,
        AddressSequentialGenerator addressSequentialGenerator,
        MultiAddressesImporter multiAddressesImporter,
        CurrentReceivingAddressesUpdater currentReceivingAddressesUpdater,
        UtxosGetter utxosGetter,
        NextChangeAddress nextChangeAddress,
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
        this.nextAddress = nextChangeAddress;
        this.currentDerivationPath = FIRST_BIP84_CHANGE_PATH;
    }

    public void clear() {
        currentDerivationPath = FIRST_BIP84_CHANGE_PATH;
        nextAddress.setReceivingAddress(
            new ReceivingAddress(ZERO, 0, "")
        );
    }
}
