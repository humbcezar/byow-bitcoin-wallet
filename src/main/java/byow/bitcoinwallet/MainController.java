package byow.bitcoinwallet;

import javafx.application.Platform;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    public void exit() {
        Platform.exit();
    }
}
