package byow.bitcoinwallet;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.repositories.WalletRepository;
import com.blockstream.libwally.Wally;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.NodeQuery;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class CreateWalletTest extends TestBase {
    @Value("classpath:/fxml/main_window.fxml")
    private Resource fxml;

    @Autowired
    ApplicationContext context;
    @Autowired
    WalletRepository walletRepository;

    @Start
    public void start (Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
        fxmlLoader.setControllerFactory(context::getBean);
        Parent root = fxmlLoader.load();
        stage.setTitle("BYOW Wallet");
        stage.setScene(new Scene(root));
        stage.show();
    }

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
        assertNotNull(text.queryLabeled().getText());
    }
}
