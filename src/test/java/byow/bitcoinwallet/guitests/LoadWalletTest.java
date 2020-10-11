package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.services.WalletCreator;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadWalletTest extends TestBase {

    @Autowired
    private WalletCreator walletCreator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
        walletCreator.create(
                "testwallet",
                "gap print mobile track security horn polar female inhale liberty general benefit",
                ""
        );
    }

    @Test
    public void loadWallet(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn("testwallet");
        assertEquals("BYOW Wallet - testwallet", stage.getTitle());
    }
}
