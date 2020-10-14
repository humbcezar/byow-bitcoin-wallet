package byow.bitcoinwallet.controllers;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

public interface BaseController {

    default void construct(Resource fxml, ApplicationContext context) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(fxml.getURL());
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(context::getBean);
        fxmlLoader.load();
    }
}
