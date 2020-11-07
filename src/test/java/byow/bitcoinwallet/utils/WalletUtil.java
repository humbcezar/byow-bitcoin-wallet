package byow.bitcoinwallet.utils;

import javafx.scene.control.TextArea;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testfx.api.FxRobot;

@Component
@Profile("test")
public class WalletUtil {
    public String createWallet(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write(RandomString.make());
        robot.clickOn("#create");
        String mnemonicSeed = robot.lookup("#mnemonicSeed").queryAs(TextArea.class).getText();
        robot.clickOn("OK");
        robot.clickOn("Receive");
        return mnemonicSeed;
    }
}
