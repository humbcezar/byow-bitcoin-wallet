package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.services.SeedGenerator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

public class ImportWalletTest extends TestBase {
    @Autowired
    SeedGenerator seedGenerator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void importBySeed(FxRobot robot) throws TimeoutException {
        String mnemonicSeed = seedGenerator.generateMnemonicSeed();
        robot.clickOn("#wallet");
        robot.clickOn("#import");
        robot.clickOn("#walletName");
        robot.write("Imported Wallet");
        robot.clickOn("#walletPassword");
        robot.write("");
        robot.clickOn("#mnemonicSeed");
        robot.write(mnemonicSeed);
        robot.clickOn("OK");
        robot.clickOn("Receive");

        assertEquals("BYOW Wallet - Imported Wallet", stage.getTitle());
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isEmpty();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertFalse(address.isEmpty());
    }
}
