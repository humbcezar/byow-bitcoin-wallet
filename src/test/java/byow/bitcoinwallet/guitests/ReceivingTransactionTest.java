package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testfx.matcher.control.TableViewMatchers.containsRow;
import static org.testfx.matcher.control.TableViewMatchers.hasNumRows;

public class ReceivingTransactionTest extends TestBase {
    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Test
    public void receiveTransaction(FxRobot robot) throws TimeoutException {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write("Receive tx wallet");
        robot.clickOn("#create");
        robot.clickOn("OK");
        robot.clickOn("Receive");
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !address.isBlank();
        });
        String address = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        bitcoindRpcClient.sendToAddress(address, BigDecimal.ONE);
        WaitForAsyncUtils.waitFor(40, TimeUnit.SECONDS, () -> {
            TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
            return tableView.getItems().size() == 1;
        });
        TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, containsRow(
                address,
                "1.00000000",
                0
            )
        );
        MatcherAssert.assertThat(tableView, hasNumRows(1));
    }
}
