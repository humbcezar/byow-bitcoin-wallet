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
    @Value("classpath:/main_window.fxml")
    private Resource fxml;
    @Autowired
    ApplicationContext context;

    @Override
    @EventListener
    public void onApplicationEvent(GuiStartedEvent guiStartedEvent) {
        FXMLLoader fxmlLoader = null;
        Parent root = null;
        try {
            fxmlLoader = new FXMLLoader(this.fxml.getURL());
            fxmlLoader.setControllerFactory(context::getBean);
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Stage stage = guiStartedEvent.getStage();
        stage.setTitle("BYOW Wallet");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
