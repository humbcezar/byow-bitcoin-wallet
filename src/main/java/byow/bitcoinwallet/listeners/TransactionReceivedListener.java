package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.address.AddressesFilter;
import byow.bitcoinwallet.services.address.InputAddressesParser;
import byow.bitcoinwallet.services.address.OutputAddressesParser;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import byow.bitcoinwallet.services.transaction.TransactionUpdater;
import byow.bitcoinwallet.tasks.TaskConfigurer;
import byow.bitcoinwallet.tasks.UpdateTransactionTask;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Component
@Lazy
public class TransactionReceivedListener implements ApplicationListener<TransactionReceivedEvent> {
    private final TransactionUpdater transactionUpdater;

    private final TaskConfigurer taskConfigurer;

    private final CurrentWallet currentWallet;

    private final AddressesFilter addressesFilter;

    private final InputAddressesParser inputAddressesParser;

    private final OutputAddressesParser outputAddressesParser;

    private final ExecutorService executorService;

    @Autowired
    public TransactionReceivedListener(
        TransactionUpdater transactionUpdater,
        TaskConfigurer taskConfigurer,
        CurrentWallet currentWallet,
        AddressesFilter addressesFilter,
        InputAddressesParser inputAddressesParser,
        OutputAddressesParser outputAddressesParser,
        ExecutorService executorService
    ) {
        this.transactionUpdater = transactionUpdater;
        this.taskConfigurer = taskConfigurer;
        this.currentWallet = currentWallet;
        this.addressesFilter = addressesFilter;
        this.inputAddressesParser = inputAddressesParser;
        this.outputAddressesParser = outputAddressesParser;
        this.executorService = executorService;
    }

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        if (currentWallet.getCurrentWallet() == null) {
            return;
        }
        List<String> inputs = inputAddressesParser.parseInputAddresses(event.getTransaction());
        List<String> outputs = outputAddressesParser.parseOutputAddresses(event.getTransaction());
        List<String> addresses = addressesFilter.filterAddresses(concat(inputs.stream(), outputs.stream())).collect(Collectors.toList());
        if (!addresses.isEmpty()) {
            executorService.submit(buildTask(event.getTransaction(), addresses, outputs));
        }
    }

    private Task<Void> buildTask(Object transaction, List<String> addresses, List<String> outputs) {
        return taskConfigurer.configure(
            new UpdateTransactionTask(transactionUpdater, transaction, addresses, outputs),
            "Receiving transaction..."
        );
    }
}
