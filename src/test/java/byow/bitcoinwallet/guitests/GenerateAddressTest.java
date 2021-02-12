package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.services.SeedGenerator;
import byow.bitcoinwallet.services.WalletCreator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.matcher.control.TableViewMatchers.containsRowAtIndex;

public class GenerateAddressTest extends TestBase {
    @Autowired
    private WalletCreator walletCreator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
        walletCreator.create("wallet", seedGenerator.generateMnemonicSeed(), "");
    }

    @Test
    public void generateSegwitAddress(FxRobot robot) {
        robot.clickOn("Receive");
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertNotNull(address);
        final TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, is(not(containsRowAtIndex(0))));
    }
}
