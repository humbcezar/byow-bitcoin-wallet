package byow.bitcoinwallet.utils;

import javafx.scene.control.TextArea;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testfx.api.FxRobot;

@Component
@Profile("test")
public class WalletUtil {
    public String createWallet(FxRobot robot, String walletName) {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write(walletName);
        robot.clickOn("#create");
        String mnemonicSeed = robot.lookup("#mnemonicSeed").queryAs(TextArea.class).getText();
        robot.clickOn("OK");
        robot.clickOn("Receive");
        return mnemonicSeed;
    }
}
