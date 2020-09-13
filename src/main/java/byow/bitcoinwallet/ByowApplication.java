package byow.bitcoinwallet;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ByowApplication {
    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
    }
}
