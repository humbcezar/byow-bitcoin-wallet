package byow.bitcoinwallet.services;

import byow.bitcoinwallet.controllers.MainController;
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
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CurrentWalletManager {

    @Autowired
    private UpdateCurrentWalletTask updateCurrentWalletTask;

    @Autowired
    private MainController mainController;

    private Wallet currentWallet;

    private final SimpleStringProperty walletName = new SimpleStringProperty();

    private final CurrentReceivingAddress currentReceivingAddress = new CurrentReceivingAddress();

    private final ObservableList<ReceivingAddress> receivingAddresses = new ObservableListWrapper<>(new LinkedList<>());

    public void updateCurrentWallet(Wallet currentWallet) {
        this.currentWallet = currentWallet;
        walletName.setValue(currentWallet.getName());
        receivingAddresses.clear();

        //TODO: progressbar in a new component
        //TODO: por task em pojo (nao componente)
        //TODO: refatorar updatecurrentwallettask para servico
        //TODO: OU criar metodo ou inner class no updatecurrentwallettask pra retornar task
        //TODO: antes de rodar task, cancelar task anterior (interrupt ou cancel, e setoncance;, colocando field task nesta classe e mudando referencia a cada execucao
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(-1, 1);
                updateCurrentWalletTask.setSeed(currentWallet.getSeed())
                        .setCurrentReceivingAddress(currentReceivingAddress)
                        .setReceivingAddresses(receivingAddresses)
                        .run();
                updateProgress(1, 1);
                return null;
            }
        };

        task.setOnScheduled(
            event ->
                mainController.getProgressBar().progressProperty().bind(task.progressProperty())
        );
        task.setOnSucceeded(event -> {
            mainController.getProgressBar().progressProperty().unbind();
            mainController.getProgressBar().progressProperty().setValue(0);
        });

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
