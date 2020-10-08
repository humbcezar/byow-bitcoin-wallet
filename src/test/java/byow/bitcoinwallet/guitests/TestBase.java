package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.factories.SpringComponentBuilderFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit5.ApplicationExtension;

@SpringBootTest
@ExtendWith(ApplicationExtension.class)
@ActiveProfiles("test")
abstract public class TestBase {
    @Value("classpath:/fxml/main_window.fxml")
    private Resource fxml;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    private SpringComponentBuilderFactory springComponentBuilderFactory;

    protected Stage stage;

    public void start (Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
        fxmlLoader.setControllerFactory(context::getBean);
        fxmlLoader.setBuilderFactory(springComponentBuilderFactory);
        Parent root = fxmlLoader.load();
        stage.setTitle("BYOW Wallet");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
