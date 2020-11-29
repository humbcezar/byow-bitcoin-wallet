package byow.bitcoinwallet.services;

import byow.bitcoinwallet.controllers.FooterController;
import byow.bitcoinwallet.controllers.ProgressBarController;
import byow.bitcoinwallet.controllers.TotalBalanceController;
import byow.bitcoinwallet.tasks.NodeMonitorTask;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class TaskConfigurer {
    private Logger logger = LoggerFactory.getLogger(NodeMonitorTask.class);

    @Autowired
    private ProgressBarController progressBarController;

    @Autowired
    private FooterController footerController;

    @Autowired
    private TotalBalanceController totalBalanceController;

    public Task<Void> configure(Task<Void> task, String text) {
        task.setOnScheduled(
            event -> {
                progressBarController.progressBar.progressProperty().bind(task.progressProperty());
                footerController.setText(text);
                totalBalanceController.update();
            }
        );
        task.setOnSucceeded(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
            footerController.setText("");
            totalBalanceController.update();
        });
        task.setOnCancelled(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
            footerController.setText("");
            totalBalanceController.update();
        });
        task.setOnFailed(event -> {
            progressBarController.progressBar.progressProperty().unbind();
            progressBarController.progressBar.progressProperty().setValue(0);
            footerController.setText("");
            totalBalanceController.update();
            try {
                throw task.getException();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return task;
    }
}
