package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.exceptions.WrongPasswordException;
import byow.bitcoinwallet.services.AuthenticationService;
import byow.bitcoinwallet.services.gui.CurrentWallet;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import org.springframework.stereotype.Component;

@Component
public class CreateWatchOnlyWalletDialogController extends GenerateWalletDialogController {
    private final CurrentWallet currentWallet;

    private final AuthenticationService authenticationService;

    @FXML
    public PasswordField currentWalletPassword;

    @FXML
    public PasswordField watchOnlyWalletPassword;

    public CreateWatchOnlyWalletDialogController(
        CurrentWallet currentWallet,
        AuthenticationService authenticationService
    ) {
        this.currentWallet = currentWallet;
        this.authenticationService = authenticationService;
    }

    @Override
    public void createWallet() {
        if (!authenticationService.checkPassword(currentWalletPassword.getText(), currentWallet.getCurrentWallet().getPassword())) {
            throw new WrongPasswordException("Could not create watch only wallet: wrong password for current wallet.");
        }
        walletCreator.createWatchOnly(
            currentWallet.getWalletName().concat("(watch only)"),
            watchOnlyWalletPassword.getText(),
            currentWallet.getCurrentWallet().getCreatedAt(),
            currentWallet.getCurrentWallet().getxPubs()
        );
    }

    @Override
    public void initialize() {
        allInputsAreFull = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return true;
            }
        };
    }
}
