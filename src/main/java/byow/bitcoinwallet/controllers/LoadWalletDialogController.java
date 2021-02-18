package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoadWalletDialogController {
    @FXML
    public PasswordField loadWalletPassword;

    private final AuthenticationService authenticationService;

    @Autowired
    public LoadWalletDialogController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public boolean passwordIsValid(String password, String hashedPassword) {
        return authenticationService.checkPassword(password, hashedPassword);
    }
}