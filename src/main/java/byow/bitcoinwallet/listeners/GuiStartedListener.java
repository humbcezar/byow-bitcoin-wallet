package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.GuiStartedEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GuiStartedListener implements ApplicationListener<GuiStartedEvent> {
    private Resource fxml;
    private ApplicationContext context;

    public GuiStartedListener(
            @Value("classpath:/fxml/main_window.fxml") Resource fxml,
            @Autowired ApplicationContext context
    ) {
        this.fxml = fxml;
        this.context = context;
    }

    @Override
    @EventListener
    public void onApplicationEvent(GuiStartedEvent guiStartedEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage stage = guiStartedEvent.getStage();
        stage.setTitle("BYOW Wallet");
        stage.setScene(new Scene(initializeFxml(fxmlLoader)));
        stage.show();
    }

    private Parent initializeFxml(FXMLLoader fxmlLoader) {
        Parent root;
        try {
            fxmlLoader.setLocation(this.fxml.getURL());
            fxmlLoader.setControllerFactory(context::getBean);
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return root;
    }
}
