package byow.bitcoinwallet;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;

@SpringBootTest
@ExtendWith(ApplicationExtension.class)
public class SimpleTest {
    @Value("classpath:/main_window.fxml")
    private Resource fxml;


    @Start
    public void start (Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
        Parent root = fxmlLoader.load();
        stage.setTitle("Hello World");
        stage.setScene(new Scene(root, 300, 275));
        stage.show();
    }

    @Test
    public void testEnglishInput (FxRobot robot) {
        robot.clickOn("#inputField");
        robot.write("This is a test!");
        robot.clickOn("#applyButton");
        FxAssert.verifyThat(".label", LabeledMatchers.hasText("This is a test!"));
    }
}
