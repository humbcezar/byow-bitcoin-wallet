package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import byow.bitcoinwallet.services.RescanAborter;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Lazy
@Component
public class UpdateCurrentWalletTask {
    private int initialAddressToMonitor;

    private UpdateCurrentWalletTaskBuilder taskBuilder;

    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    private RescanAborter rescanAborter;

    private String seed;

    private UpdateTask currentTask;

    private Date walletCreationDate;

    @Autowired
    public UpdateCurrentWalletTask(
            UpdateCurrentWalletTaskBuilder taskBuilder,
            CurrentReceivingAddressesManager currentReceivingAddressesManager,
            RescanAborter rescanAborter
    ) {
        this.taskBuilder = taskBuilder;
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        this.rescanAborter = rescanAborter;
    }

    public void update() {
        List<String> addressList = currentReceivingAddressesManager.initializeReceivingAddresses(initialAddressToMonitor, seed, walletCreationDate);
        int updatedAddressesCount = currentReceivingAddressesManager.updateReceivingAddresses(addressList);
        if (updatedAddressesCount >= initialAddressToMonitor) {
            currentReceivingAddressesManager.setNextCurrentDerivationPath(initialAddressToMonitor);
            update();
            return;
        }
        currentReceivingAddressesManager.updateNextAddress(addressList.get(0), updatedAddressesCount, seed);
    }

    public UpdateTask getTask() {
        currentTask = taskBuilder.build(new UpdateTask());
        return currentTask;
    }

    public void cancel() {
        if (currentTask != null) {
            currentTask.cancel();
            rescanAborter.abortRescan();
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

    public UpdateCurrentWalletTask setDate(Date createdAt) {
        walletCreationDate = createdAt;
        return this;
    }

    @Autowired
    public void setInitialAddressToMonitor(@Value("${bitcoin.initial_addresses_to_monitor}") int initialAddressToMonitor) {
        this.initialAddressToMonitor = initialAddressToMonitor;
    }
}
