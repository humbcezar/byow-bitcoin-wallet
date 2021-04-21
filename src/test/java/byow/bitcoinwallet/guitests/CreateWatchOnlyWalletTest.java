package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.utils.WalletUtil;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;

import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.bytebuddy.utility.RandomString.make;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class CreateWatchOnlyWalletTest extends TestBase {

    @Autowired
    private WalletUtil walletUtil;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void createWatchOnlyWallet(FxRobot robot) throws TimeoutException {
        String walletName = make();
        walletUtil.createWallet(robot, walletName, "");
        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).equals(stage.getTitle()));
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddress = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        robot.clickOn("#wallet");
        robot.clickOn("#watchOnly");

        robot.clickOn("#currentWalletPassword");
        robot.clickOn("#watchOnlyWalletPassword");
        robot.clickOn("OK");

        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).concat("(watch only)").equals(stage.getTitle()));
        assertEquals("BYOW Wallet - ".concat(walletName).concat("(watch only)"), stage.getTitle());

        robot.clickOn("Receive");
        String addressWatch = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddressWatch = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        assertEquals(addressWatch, address);
        assertEquals(nestedAddressWatch, nestedAddress);
    }

    @Test
    public void createWatchOnlyWalletWithPassword(FxRobot robot) throws TimeoutException {
        String walletName = make();
        walletUtil.createWallet(robot, walletName, "");
        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).equals(stage.getTitle()));
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddress = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        robot.clickOn("#wallet");
        robot.clickOn("#watchOnly");

        robot.clickOn("#currentWalletPassword");
        robot.clickOn("#watchOnlyWalletPassword");
        robot.write("asdf");
        robot.clickOn("OK");

        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).concat("(watch only)").equals(stage.getTitle()));
        assertEquals("BYOW Wallet - ".concat(walletName).concat("(watch only)"), stage.getTitle());

        robot.clickOn("Receive");
        String addressWatch = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddressWatch = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        assertEquals(addressWatch, address);
        assertEquals(nestedAddressWatch, nestedAddress);
    }

    @Test
    public void createWatchOnlyWalletWithParentWalletWithPassword(FxRobot robot) throws TimeoutException {
        String walletName = make();
        String password = RandomString.make();
        walletUtil.createWallet(robot, walletName, password);
        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).equals(stage.getTitle()));
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddress = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        robot.clickOn("#wallet");
        robot.clickOn("#watchOnly");

        robot.clickOn("#currentWalletPassword");
        robot.write(password);
        robot.clickOn("#watchOnlyWalletPassword");
        robot.write("asdf");
        robot.clickOn("OK");

        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).concat("(watch only)").equals(stage.getTitle()));
        assertEquals("BYOW Wallet - ".concat(walletName).concat("(watch only)"), stage.getTitle());

        robot.clickOn("Receive");
        String addressWatch = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddressWatch = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        assertEquals(addressWatch, address);
        assertEquals(nestedAddressWatch, nestedAddress);
    }

    @Test
    public void createWatchOnlyWalletWithParentWalletWithWrongPasswordFail(FxRobot robot) throws TimeoutException {
        String walletName = make();
        String password = RandomString.make();
        walletUtil.createWallet(robot, walletName, password);
        waitFor(60, SECONDS, () -> "BYOW Wallet - ".concat(walletName).equals(stage.getTitle()));
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        String nestedAddress = robot.lookup("#nestedReceivingAddress").queryAs(TextField.class).getText();

        robot.clickOn("#wallet");
        robot.clickOn("#watchOnly");

        robot.clickOn("#currentWalletPassword");
        robot.write("iii");
        robot.clickOn("#watchOnlyWalletPassword");
        robot.write("asdf");
        robot.clickOn("OK");

        NodeQuery text = robot.lookup(
            "Could not create watch only wallet: wrong password for current wallet."
        );
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }
}
