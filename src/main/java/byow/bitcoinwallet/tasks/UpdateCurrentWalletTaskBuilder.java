package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.controllers.ProgressBarController;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask.UpdateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class UpdateCurrentWalletTaskBuilder {
    private Logger logger = LoggerFactory.getLogger(TransactionTask.class);

    @Autowired
    private ProgressBarController progressBarController;

    @Autowired
    private TransactionTask transactionTask;

    public UpdateTask build(UpdateTask task) {
        task.setOnScheduled(
                event -> progressBarController.progressBar.progressProperty().bind(task.progressProperty())
        );
        task.setOnSucceeded(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
            transactionTask.subscribe();
        });
        task.setOnCancelled(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
            transactionTask.unsubscribe();
        });
        task.setOnFailed(event -> task.getException().printStackTrace());
        return task;
    }
}
