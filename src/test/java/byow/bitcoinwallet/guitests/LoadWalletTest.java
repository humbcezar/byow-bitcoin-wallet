package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.services.SeedGenerator;
import byow.bitcoinwallet.services.WalletCreator;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.matcher.control.TableViewMatchers.containsRowAtIndex;

public class LoadWalletTest extends TestBase {

    @Autowired
    private WalletCreator walletCreator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
        walletCreator.create("testwallet", seedGenerator.generateMnemonicSeed(), "");
    }

    @Test
    public void loadWallet(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn("testwallet");
        assertEquals("BYOW Wallet - testwallet", stage.getTitle());
        final TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, is(not(containsRowAtIndex(0))));
    }
}
