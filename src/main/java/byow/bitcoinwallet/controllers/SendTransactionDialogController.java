package byow.bitcoinwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

@Component
public class SendTransactionDialogController {
    @FXML
    public Label amountToSend;

    @FXML
    private Label addressToSend;

    public void setAmountToSend(String amount) {
        amountToSend.setText(amount);
    }

    public void setAddressToSend(String address) {
        addressToSend.setText(address);
    }
}
