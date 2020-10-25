package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.AddressSequentialGenerator;
import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import byow.bitcoinwallet.services.MultiAddressesImporter;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.util.List;

@Lazy
@Component
public class UpdateCurrentWalletTask {
    private int initialAddressToMonitor;

    private MultiAddressesImporter multiAddressesImporter;

    private AddressSequentialGenerator addressSequentialGenerator;

    private BitcoindRpcClient bitcoindRpcClient;

    private UpdateCurrentWalletTaskBuilder taskBuilder;

    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private String seed;

    private UpdateTask currentTask;

    @Autowired
    public UpdateCurrentWalletTask(
            MultiAddressesImporter multiAddressesImporter,
            AddressSequentialGenerator addressSequentialGenerator,
            BitcoindRpcClient bitcoindRpcClient,
            UpdateCurrentWalletTaskBuilder taskBuilder,
            CurrentReceivingAddressesManager currentReceivingAddressesManager
    ) {
        this.multiAddressesImporter = multiAddressesImporter;
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.bitcoindRpcClient = bitcoindRpcClient;
        this.taskBuilder = taskBuilder;
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
    }

    public void update() {
        List<String> addressList = initializeAddresses();
        List<Unspent> utxos = getUtxos(addressList);
        int updatedAddressesCount = currentReceivingAddressesManager.updateReceivingAddresses(utxos);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            currentReceivingAddressesManager.setNextCurrentDerivationPath(initialAddressToMonitor);
            update();
            return;
        }
        currentReceivingAddressesManager.updateNextAddress(addressList.get(0), updatedAddressesCount, seed);
    }

    private List<String> initializeAddresses() {
        List<String> addressList = addressSequentialGenerator.deriveAddresses(
            initialAddressToMonitor,
            seed,
            currentReceivingAddressesManager.getCurrentDerivationPath()
        );
        currentReceivingAddressesManager.initializeReceivingAddresses(addressList);
        return addressList;
    }

    private List<Unspent> getUtxos(List<String> addressList) {
        multiAddressesImporter.importMultiAddresses(addressList.toArray(new String[0]));
        return bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
    }

    public UpdateTask getTask() {
        currentTask = taskBuilder.build(new UpdateTask());
        return currentTask;
    }

    public void cancel() {
        if (currentTask != null) {
            currentTask.cancel();
        }
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

    public UpdateCurrentWalletTask setSeed(String seed) {
        this.seed = seed;
        return this;
    }

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }
}
