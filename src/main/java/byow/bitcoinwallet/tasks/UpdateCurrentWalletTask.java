package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressSequentialGenerator;
import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import byow.bitcoinwallet.services.DerivationPath;
import byow.bitcoinwallet.services.MultiAddressesImporter;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
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

    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private String seed;

    private NextReceivingAddress nextReceivingAddress;

    private DerivationPath currentDerivationPath = FIRST_BIP84_ADDRESS_PATH;

    @Autowired
    public UpdateCurrentWalletTask(
            MultiAddressesImporter multiAddressesImporter,
            AddressSequentialGenerator addressSequentialGenerator,
            BitcoindRpcClient bitcoindRpcClient,
            UpdateCurrentWalletTaskBuilder taskBuilder,
            CurrentReceivingAddressesManager currentReceivingAddressesManager,
            NextReceivingAddress nextReceivingAddress
    ) {
        this.multiAddressesImporter = multiAddressesImporter;
        this.addressSequentialGenerator = addressSequentialGenerator;
        this.bitcoindRpcClient = bitcoindRpcClient;
        this.taskBuilder = taskBuilder;
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        this.nextReceivingAddress = nextReceivingAddress;
    }

    public void update() {
        nextReceivingAddress.setReceivingAddress(
            new ReceivingAddress(BigDecimal.ZERO, 0, "")
        );
        List<String> addressList = initializeAddresses();
        List<Unspent> utxos = getUtxos(addressList);
        int updatedAddressesCount = currentReceivingAddressesManager.updateReceivingAddresses(utxos);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            currentDerivationPath = currentDerivationPath.next(initialAddressToMonitor);
            update();
            return;
        }
        updateNextAddress(addressList, updatedAddressesCount);
    }

    private List<String> initializeAddresses() {
        List<String> addressList = addressSequentialGenerator.deriveAddresses(
            initialAddressToMonitor,
            seed,
            currentDerivationPath
        );
        currentReceivingAddressesManager.initializeReceivingAddresses(addressList);
        return addressList;
    }

    private void updateNextAddress(List<String> addressList, int updatedAddressesCount) {
        String nextAddress = addressList.get(0);
        if (updatedAddressesCount > 0) {
            nextAddress = addressSequentialGenerator.deriveAddresses(
                    1,
                    seed,
                    currentDerivationPath.next(updatedAddressesCount)
            ).get(0);
        }
        nextReceivingAddress.setReceivingAddress(
            new ReceivingAddress(BigDecimal.ZERO, 0, nextAddress)
        );
    }

    private List<Unspent> getUtxos(List<String> addressList) {
        multiAddressesImporter.importMultiAddresses(addressList.toArray(new String[0]));
        return bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
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

    public UpdateCurrentWalletTask setSeed(String seed) {
        this.seed = seed;
        return this;
    }

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }
}
