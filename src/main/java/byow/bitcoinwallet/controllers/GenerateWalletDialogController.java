package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.wallet.WalletCreator;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GenerateWalletDialogController {
    @FXML
    public TextField walletName;

    @FXML
    public TextArea mnemonicSeed;

    @FXML
    public PasswordField walletPassword;

    @Autowired
    protected WalletCreator walletCreator;

    protected BooleanBinding allInputsAreFull;

    public void createWallet() {
        walletCreator.create(walletName.getText(), mnemonicSeed.getText(), walletPassword.getText());
        mnemonicSeed.clear();
        walletPassword.clear();
        walletName.clear();
    }

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
}
