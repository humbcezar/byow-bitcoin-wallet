package byow.bitcoinwallet.events;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class GuiStartedEvent extends ApplicationEvent {
    private final Stage stage;

    public GuiStartedEvent(Object source, Stage stage) {
        super(source);
        this.stage = stage;
    }

    public Stage getStage() {
        return this.stage;
    }
}
