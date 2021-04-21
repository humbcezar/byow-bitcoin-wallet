package byow.bitcoinwallet.utils;

import javafx.scene.control.TextArea;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.testfx.api.FxRobot;
import javafx.stage.Stage;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

@Component
@Profile("test")
public class WalletUtil {
    public String createWallet(FxRobot robot, String walletName, String password) {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write(walletName);
        if (!password.isEmpty()) {
            robot.clickOn("#walletPassword");
            robot.write(password);
        }
        robot.clickOn("#create");
        String mnemonicSeed = robot.lookup("#mnemonicSeed").queryAs(TextArea.class).getText();
        robot.clickOn("OK");
        robot.clickOn("Receive");
        return mnemonicSeed;
    }

    public String createWatchOnlyWallet(FxRobot robot, String name, String password, Stage stage) throws TimeoutException {
        String mnemonicSeed = createWallet(robot, name, password);
        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(name).equals(stage.getTitle()));

        robot.clickOn("#wallet");
        robot.clickOn("#watchOnly");

        robot.clickOn("#currentWalletPassword");
        robot.clickOn("#watchOnlyWalletPassword");
        robot.clickOn("OK");

        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(name).concat("(watch only)").equals(stage.getTitle()));
        return mnemonicSeed;
    }
}
