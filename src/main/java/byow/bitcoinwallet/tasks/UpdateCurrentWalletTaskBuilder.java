package byow.bitcoinwallet.tasks;

import byow.bitcoinwallet.controllers.ProgressBarController;
import byow.bitcoinwallet.tasks.UpdateCurrentWalletTask.UpdateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class UpdateCurrentWalletTaskBuilder {
    @Autowired
    private ProgressBarController progressBarController;

    public UpdateTask build(UpdateTask task) {
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
}
