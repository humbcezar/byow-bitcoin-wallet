package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.services.address.SeedGenerator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

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

        waitFor(40, SECONDS, () ->
            "BYOW Wallet - Imported Wallet".equals(stage.getTitle())
        );
        assertEquals("BYOW Wallet - Imported Wallet", stage.getTitle());
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isEmpty();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertFalse(address.isEmpty());
    }
}
