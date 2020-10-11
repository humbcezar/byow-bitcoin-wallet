package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.AddressGenerator;
import byow.bitcoinwallet.services.WalletCreator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.TableViewMatchers;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;


public class BalanceTableTest extends TestBase {

    @Autowired
    private WalletCreator walletCreator;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private AddressGenerator addressGenerator;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        initMocks(this);
        walletCreator.setApplicationEventPublisher(this.applicationEventPublisher);
        walletCreator.create(
                "testwallet2",
                "gap print mobile track security horn polar female inhale liberty general benefit",
                ""
        );
        String address = bitcoindRpcClient.getNewAddress();
        bitcoindRpcClient.generateToAddress(101, address);
        bitcoindRpcClient.sendToAddress("bcrt1qzvfaa0r54fnlfsdv745h2npe7uuwph08d9n5a9", BigDecimal.ONE);
        bitcoindRpcClient.generateToAddress(1, address);
        super.start(stage);
    }

    @Test
    public void showAddressWithPositiveBalance(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn("testwallet2");
        final TableView tableView = robot.lookup("#balanceTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, TableViewMatchers.containsRowAtIndex(
                0,
                "bcrt1qzvfaa0r54fnlfsdv745h2npe7uuwph08d9n5a9",
                "1.00000000",
                1
            )
        );
    }
}
