package byow.bitcoinwallet;

import byow.bitcoinwallet.events.GuiStartedEvent;
import byow.bitcoinwallet.tasks.NodeMonitorTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class GuiApplication extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        this.context = SpringApplication.run(ByowApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.context.publishEvent(new GuiStartedEvent(this, stage));
    }

    @Override
    public void stop() throws Exception {
        this.context.getBean(NodeMonitorTask.class).close();
        this.context.close();
        Platform.exit();
    }
}
