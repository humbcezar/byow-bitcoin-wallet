package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.Wallet;
import byow.bitcoinwallet.services.SeedGenerator;
import byow.bitcoinwallet.services.WalletCreator;
import javafx.beans.binding.BooleanBinding;
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

    @FXML
    public TextField walletPassword;

    @Autowired
    private WalletCreator walletCreator;

    @Autowired
    private SeedGenerator seedGenerator;

    private BooleanBinding allInputsAreFull;

    public BooleanBinding getAllInputsAreFull() {
        return allInputsAreFull;
    }

    public void initialize() {
        allInputsAreFull = new BooleanBinding() {
            {
                bind(walletName.textProperty(), mnemonicSeed.textProperty());
            }

            @Override
            protected boolean computeValue() {
                return !(walletName.getText().trim().isEmpty() || mnemonicSeed.getText().trim().isEmpty());
            }
        };
    }

    public void generateMnemonicSeed() {
        mnemonicSeed.setText(seedGenerator.generateMnemonicSeed());
    }

    public void createWallet() {
        walletCreator.create(walletName.getText(), mnemonicSeed.getText(), walletPassword.getText());
    }
}
