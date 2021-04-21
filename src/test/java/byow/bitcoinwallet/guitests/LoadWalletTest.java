package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.TransactionRepository;
import byow.bitcoinwallet.repositories.WalletRepository;
import byow.bitcoinwallet.repositories.XPubRepository;
import byow.bitcoinwallet.services.address.SeedGenerator;
import byow.bitcoinwallet.services.wallet.WalletCreator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.internal.bytebuddy.utility.RandomString.make;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.matcher.control.TableViewMatchers.containsRowAtIndex;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class LoadWalletTest extends TestBase {

    private static final long TIMEOUT = 40;
    @Autowired
    private WalletCreator walletCreator;

    @Autowired
    private SeedGenerator seedGenerator;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private XPubRepository xPubRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private String password;

    private String testWallet;

    private String testWalletWithPassword;

    private String testWatchWalletWithPassword;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        xPubRepository.deleteAll();
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        super.start(stage);
        testWallet = make();
        testWalletWithPassword = make();
        testWatchWalletWithPassword = make();
        walletCreator.create(testWallet, seedGenerator.generateMnemonicSeed(), "");
        password = make();
        walletCreator.create(testWalletWithPassword, seedGenerator.generateMnemonicSeed(), password);
    }

    @Test
    public void loadWallet(FxRobot robot) throws TimeoutException {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(testWallet);
        robot.clickOn("OK");
        waitFor(40, SECONDS, () ->
            "BYOW Wallet - ".concat(testWallet).equals(stage.getTitle())
        );
        assertEquals("BYOW Wallet - ".concat(testWallet), stage.getTitle());
        final TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, is(not(containsRowAtIndex(0))));

        robot.clickOn("Receive");
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !nextAddress.isEmpty();
        });
        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertFalse(nextAddress.isEmpty());
    }

    @Test
    public void loadWalletWithPassword(FxRobot robot) throws TimeoutException {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(testWalletWithPassword);
        robot.clickOn("#loadWalletPassword");
        robot.write(password);
        robot.clickOn("OK");
        waitFor(40, SECONDS, () ->
            "BYOW Wallet - ".concat(testWalletWithPassword).equals(stage.getTitle())
        );
        assertEquals("BYOW Wallet - ".concat(testWalletWithPassword), stage.getTitle());
        final TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, is(not(containsRowAtIndex(0))));

        robot.clickOn("Receive");
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !nextAddress.isEmpty();
        });
        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertFalse(nextAddress.isEmpty());
    }

    @Test
    public void loadWatchOnlyWalletWithPassword(FxRobot robot) throws TimeoutException {
        Wallet wallet = walletRepository.findByName(testWalletWithPassword);
        walletCreator.createWatchOnly(testWatchWalletWithPassword, password, new Date(), wallet.getxPubs());

        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(testWatchWalletWithPassword);
        robot.clickOn("#loadWalletPassword");
        robot.write(password);
        robot.clickOn("OK");
        waitFor(40, SECONDS, () ->
            "BYOW Wallet - ".concat(testWatchWalletWithPassword).equals(stage.getTitle())
        );
        assertEquals("BYOW Wallet - ".concat(testWatchWalletWithPassword), stage.getTitle());
        final TableView tableView = robot.lookup("#addressesTable").queryAs(TableView.class);
        MatcherAssert.assertThat(tableView, is(not(containsRowAtIndex(0))));

        robot.clickOn("Receive");
        waitFor(TIMEOUT, TimeUnit.SECONDS, () -> {
            String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
            return !nextAddress.isEmpty();
        });
        String nextAddress = robot.lookup("#receivingAddress").queryAs(TextField.class).getText();
        assertFalse(nextAddress.isEmpty());
    }

    @Test
    public void loadWalletWithWrongPasswordFail(FxRobot robot) throws TimeoutException {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn(testWalletWithPassword);
        robot.clickOn("#loadWalletPassword");
        robot.write("gibberish");
        robot.clickOn("OK");
        NodeQuery text = robot.lookup("Wrong password.");
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }

    @Test
    public void loadWatchWalletWithWrongPasswordFail(FxRobot robot) throws TimeoutException {
        Wallet wallet = walletRepository.findByName(testWallet);
        walletCreator.createWatchOnly("testWatchWalletWithWrongPassword", "asdf", new Date(), wallet.getxPubs());
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn("testWatchWalletWithWrongPassword");
        robot.clickOn("#loadWalletPassword");
        robot.write("gibberish");
        robot.clickOn("OK");
        NodeQuery text = robot.lookup("Wrong password.");
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }
}
