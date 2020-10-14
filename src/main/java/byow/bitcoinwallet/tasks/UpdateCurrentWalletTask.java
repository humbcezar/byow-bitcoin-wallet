package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.controllers.ProgressBarController;
import byow.bitcoinwallet.entities.CurrentReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressGenerator;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import static byow.bitcoinwallet.services.DerivationPath.FIRST_BIP84_ADDRESS_PATH;

@Lazy
@Component
public class UpdateCurrentWalletTask {
    @Value("${bitcoin.initial_addresses_to_monitor}")
    private int initialAddressToMonitor;

    @Autowired
    private MultiAddressesImporter multiAddressesImporter;

    @Autowired
    private AddressGenerator addressGenerator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private ProgressBarController progressBarController;

    private String seed;

    private ObservableList<ReceivingAddress> receivingAddresses;

    private CurrentReceivingAddress currentReceivingAddress;

    public void update() {
        List<String> addressList = deriveAddresses();
        List<Unspent> utxos = getUtxos(addressList);
        setUsedAddresses(utxos);
        setNextAddress(addressList, utxos);
    }

    private void setNextAddress(List<String> addressList, List<Unspent> utxos) {
        String lastUsedAddress = utxos.get(utxos.size() - 1).address();
        currentReceivingAddress.setReceivingAddress(
                new ReceivingAddress(BigDecimal.ZERO, 0, addressList.get(addressList.indexOf(lastUsedAddress) + 1))
        );
    }

    private void setUsedAddresses(List<Unspent> utxos) {
        LinkedHashMap<String, ReceivingAddress> usedReceivingAddressMap = new LinkedHashMap<>();
        utxos.forEach(
                utxo -> usedReceivingAddressMap.putIfAbsent(utxo.address(), buildReceivingAddress(usedReceivingAddressMap, utxo))
        );
        receivingAddresses.setAll(usedReceivingAddressMap.values());
    }

    private List<Unspent> getUtxos(List<String> addressList) {
        multiAddressesImporter.importMultiAddresses(addressList.toArray(new String[0]));
        List<Unspent> utxos = bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
        return utxos;
    }

    private List<String> deriveAddresses() {
        List<String> addressList = new LinkedList<>();
        DerivationPath addressPath = FIRST_BIP84_ADDRESS_PATH;
        for (int i = 0; i < initialAddressToMonitor; i++) {
            addressList.add(addressGenerator.generate(seed, addressPath));
            addressPath = addressPath.next();
        }
        return addressList;
    }

    public UpdateTask getTask() {
        UpdateTask task = new UpdateTask();
        task.setOnScheduled(
                event -> progressBarController.progressBar.progressProperty().bind(task.progressProperty())
        );
        task.setOnSucceeded(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
        });
        task.setOnCancelled(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
        });
        return task;
    }

    private class UpdateTask extends Task<Void> {
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
        address.setBalance(utxo.amount() + address.getBalance());
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
}
