package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.controllers.MainController;
import byow.bitcoinwallet.services.WalletCreator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GenerateAddressTest extends TestBase {
    @Autowired
    WalletCreator walletCreator;

    @Autowired
    MainController mainController;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
        walletCreator.create("wallet", walletCreator.generateMnemonicSeed(), "");
    }

    @Test
    public void generateSegwitAddress(FxRobot robot) {
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertNotNull(address);
    }
}
