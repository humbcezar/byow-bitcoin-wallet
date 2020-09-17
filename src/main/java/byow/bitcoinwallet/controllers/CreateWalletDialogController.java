package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.WalletCreator;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateWalletDialogController {
    @FXML
    public TextField walletName;
    @FXML
    public TextArea mnemonicSeed;
    @Autowired
    private WalletCreator walletCreator;

    public void generateMnemonicSeed() {
        String seed = walletCreator.generateMnemonicSeed();
        mnemonicSeed.setText(seed);
    }

    public void createWallet() {
        walletCreator.create(walletName.getText(), mnemonicSeed.getText());
    }
}
