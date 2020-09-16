package byow.bitcoinwallet;

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

public class CreateWalletTest extends TestBase {
    @Value("classpath:/main_window.fxml")
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
        String langEn = Wally.bip39_get_languages().split(" ")[0];
        Object wordList = Wally.bip39_get_wordlist(langEn);
        Wallet wallet = walletRepository.findByName("Test wallet");
        // TODO: setar variaveis de banco diferentes e create destroy ddlauto
        // TODO: criar teste de repositorio
        try {
            Wally.bip39_mnemonic_validate(wordList, wallet.getMnemonicSeed());
        } catch (final Exception e) {
            Assertions.fail(e);
        }
        Assertions.assertEquals(mnemonicSeed, wallet.getMnemonicSeed());
    }
}
