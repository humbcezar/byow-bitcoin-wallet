package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.CurrentReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressSequentialGenerator;
import byow.bitcoinwallet.services.DerivationPath;
import byow.bitcoinwallet.services.MultiAddressesImporter;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Lazy
@Component
public class UpdateCurrentWalletTask {
    private int initialAddressToMonitor;

    private MultiAddressesImporter multiAddressesImporter;

    private AddressSequentialGenerator addressSequentialGenerator;

    private BitcoindRpcClient bitcoindRpcClient;

    private UpdateCurrentWalletTaskBuilder taskBuilder;

    private String seed;

    private ObservableList<ReceivingAddress> receivingAddresses;

    private CurrentReceivingAddress currentReceivingAddress;

    private DerivationPath currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;

    @Autowired
    public UpdateCurrentWalletTask(
            MultiAddressesImporter multiAddressesImporter,
            AddressSequentialGenerator addressSequentialGenerator,
            BitcoindRpcClient bitcoindRpcClient,
            UpdateCurrentWalletTaskBuilder taskBuilder
    ) {
        this.multiAddressesImporter = multiAddressesImporter;
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.bitcoindRpcClient = bitcoindRpcClient;
        this.taskBuilder = taskBuilder;
    }

    public void update() {
        List<String> addressList = addressSequentialGenerator.deriveAddresses(
                initialAddressToMonitor,
                seed,
                currentDerivationPath
        );
        List<Unspent> utxos = getUtxos(addressList);
        Collection<ReceivingAddress> receivingAddressCollection = updateUsedAddresses(utxos);
        if (receivingAddressCollection.size() >= initialAddressToMonitor) {
            currentDerivationPath = currentDerivationPath.next(initialAddressToMonitor);
            update();
            return;
        }
        updateNextAddress(addressList, receivingAddressCollection);
    }

    private void updateNextAddress(List<String> addressList, Collection<ReceivingAddress> receivingAddressCollection) {
        String nextAddress = addressList.get(0);
        if (receivingAddressCollection.size() > 0) {
            String lastUsedAddress = receivingAddressCollection.stream()
                    .skip(receivingAddressCollection.size() - 1)
                    .findFirst()
                    .get()
                    .getAddress();
            nextAddress = addressList.get(addressList.indexOf(lastUsedAddress) + 1);
        }
        currentReceivingAddress.setReceivingAddress(
                new ReceivingAddress(BigDecimal.ZERO, 0, nextAddress)
        );
    }

    private Collection<ReceivingAddress> updateUsedAddresses(List<Unspent> utxos) {
        LinkedHashMap<String, ReceivingAddress> usedReceivingAddressMap = new LinkedHashMap<>();
        utxos.forEach(
                utxo -> usedReceivingAddressMap.putIfAbsent(utxo.address(), buildReceivingAddress(usedReceivingAddressMap, utxo))
        );
        receivingAddresses.addAll(usedReceivingAddressMap.values());
        return usedReceivingAddressMap.values();
    }

    private List<Unspent> getUtxos(List<String> addressList) {
        multiAddressesImporter.importMultiAddresses(addressList.toArray(new String[0]));
        List<Unspent> utxos = bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
        return utxos;
    }

    public UpdateTask getTask() {
        return taskBuilder.build(new UpdateTask());
    }

    class UpdateTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            updateProgress(-1, 1);
            update();
            updateProgress(1, 1);
            return null;
        }
    }

    private ReceivingAddress buildReceivingAddress(LinkedHashMap<String, ReceivingAddress> receivingAddresses, Unspent utxo) {
        ReceivingAddress address = receivingAddresses.getOrDefault(utxo.address(), null);
        if (address == null) {
            return new ReceivingAddress(utxo.amount(), utxo.confirmations(), utxo.address());
        }
        if (utxo.confirmations() < address.getConfirmations()) {
            address.setConfirmations(utxo.confirmations());
        }
        address.setBalance(utxo.amount().add(new BigDecimal(address.getBalance())).toString());
        return address;
    }

    public UpdateCurrentWalletTask setSeed(String seed) {
        this.seed = seed;
        return this;
    }

    public UpdateCurrentWalletTask setReceivingAddresses(ObservableList<ReceivingAddress> receivingAddresses) {
        this.receivingAddresses = receivingAddresses;
        return this;
    }

    public UpdateCurrentWalletTask setCurrentReceivingAddress(CurrentReceivingAddress currentReceivingAddress) {
        this.currentReceivingAddress = currentReceivingAddress;
        return this;
    }

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }

}
