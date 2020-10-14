package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.CurrentReceivingAddress;
import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Lazy
@Component
public class CurrentWalletManager {

    @Autowired
    private UpdateCurrentWalletTask updateCurrentWalletTask;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    private final CurrentReceivingAddress currentReceivingAddress = new CurrentReceivingAddress();

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>());

    private Task<Void> task;

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        receivingAddresses.clear();
        currentReceivingAddress.setReceivingAddress(new ReceivingAddress(BigDecimal.ZERO, 0, ""));

        if (task != null) {
            task.cancel();
        }
        task = updateCurrentWalletTask.setSeed(currentWallet.getSeed())
                        .setCurrentReceivingAddress(currentReceivingAddress)
                        .setReceivingAddresses(receivingAddresses)
                        .getTask();

        new Thread(task).start();
    }

    public String getWalletName() {
        return walletName.get();
    }

    public SimpleStringProperty walletNameProperty() {
        return walletName;
    }

    public ObservableList<ReceivingAddress> getReceivingAddresses() {
        return receivingAddresses;
    }

    public ReceivingAddress getCurrentReceivingAddress() {
        return currentReceivingAddress.getValue();
    }

    public ObservableValue<ReceivingAddress> currentReceivingAddressProperty() {
        return currentReceivingAddress;
    }
}
