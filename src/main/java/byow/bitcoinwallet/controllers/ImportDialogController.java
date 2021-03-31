package byow.bitcoinwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.time.ZoneOffset.UTC;
import static java.util.Date.from;
import static java.util.Objects.isNull;

@Component
public class ImportDialogController extends GenerateWalletDialogController {
    @FXML
    public DatePicker creationDate;

    @Override
    public void createWallet() {
        walletCreator.create(
            walletName.getText(),
            mnemonicSeed.getText(),
            walletPassword.getText(),
            buildDate()
        );
        mnemonicSeed.clear();
        walletPassword.clear();
        walletName.clear();
    }

    private Date buildDate() {
        Date date = null;
        if (!isNull(creationDate.getValue())) {
            date = from(creationDate.getValue().atStartOfDay().toInstant(UTC));
        }
        return date;
    }
}
