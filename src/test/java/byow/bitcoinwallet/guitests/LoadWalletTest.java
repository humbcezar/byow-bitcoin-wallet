package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.repositories.WalletRepository;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadWalletTest extends TestBase {
    @Autowired
    WalletRepository walletRepository;

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        Wallet wallet = new Wallet("testwallet", "abc");
        walletRepository.save(wallet);
        super.start(stage);
    }

    @Test
    public void loadWallet(FxRobot robot) {
        robot.clickOn("#wallet");
        robot.moveTo("#load");
        robot.clickOn("testwallet");
        assertEquals("BYOW Wallet - testwallet", stage.getTitle());
    }
}
