package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.blockstream.libwally.Wally;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class CreateWalletTest extends TestBase {
    @Override
    @Start
    public void start(Stage stage) throws Exception {
        super.start(stage);
    }

    @Autowired
    WalletRepository walletRepository;

    @Test
    public void createWallet (FxRobot robot) throws InterruptedException {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write("Test wallet");
        robot.clickOn("#create");
        String mnemonicSeed = robot.lookup("#mnemonicSeed").queryAs(TextArea.class).getText();
        robot.clickOn("OK");
        Object wordList = Wally.bip39_get_wordlist(Languages.EN);
        Wallet wallet = walletRepository.findByName("Test wallet");
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
        } catch (final Exception e) {
            Assertions.fail(e);
        }
        assertEquals("Test wallet", wallet.getName());
        assertTrue(wallet.getSeed() != null && !wallet.getSeed().isEmpty());
        assertEquals("BYOW Wallet - Test wallet", stage.getTitle());
    }

    @Test
    public void createWalletWithPassword (FxRobot robot) throws InterruptedException {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#walletName");
        robot.write("Test wallet3");
        robot.clickOn("#walletPassword");
        robot.write("password");
        robot.clickOn("#create");
        String mnemonicSeed = robot.lookup("#mnemonicSeed").queryAs(TextArea.class).getText();
        robot.clickOn("OK");
        Object wordList = Wally.bip39_get_wordlist(Languages.EN);
        Wallet wallet = walletRepository.findByName("Test wallet3");
        try {
            Wally.bip39_mnemonic_validate(wordList, mnemonicSeed);
        } catch (final Exception e) {
            Assertions.fail(e);
        }
        assertEquals("Test wallet3", wallet.getName());
        assertTrue(wallet.getSeed() != null && !wallet.getSeed().isEmpty());
        assertEquals("BYOW Wallet - Test wallet3", stage.getTitle());
    }

    @Test
    public void createWalletWithoutNameFails(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.clickOn("#new");
        robot.clickOn("#create");
        robot.clickOn("OK");
        robot.clickOn("Cancel");
    }

    @Test
    public void createWalletWithRepeatedNameFails(FxRobot robot) {
        IntStream.range(0, 2).forEach(i -> {
            robot.clickOn("#wallet");
            robot.clickOn("#new");
            robot.clickOn("#walletName");
            robot.write("Test wallet2");
            robot.clickOn("#create");
            robot.clickOn("OK");
        });
        NodeQuery text = robot.lookup(
            "Could not create wallet: A wallet with the same name already exists."
        );
        robot.clickOn("OK");
        assertNotNull(text.queryLabeled().getText());
    }
}
