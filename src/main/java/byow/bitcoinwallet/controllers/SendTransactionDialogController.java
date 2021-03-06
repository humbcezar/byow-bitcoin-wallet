package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.services.AuthenticationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.UNNECESSARY;

@Component
public class SendTransactionDialogController {
    @FXML
    public Label amountToSend;

    @FXML
    public Label totalFee;

    @FXML
    public Label total;

    @FXML
    public Label feeRate;

    @FXML
    public PasswordField sendTransactionPassword;

    @FXML
    private Label addressToSend;

    private final AuthenticationService authenticationService;

    @Autowired
    public SendTransactionDialogController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    private void buildAmountToSend(String amount) {
        BigDecimal value = new BigDecimal(amount).setScale(8, UNNECESSARY);
        amountToSend.setText(value.toString().concat(" BTC"));
    }

    private void buildAddressToSend(String address) {
        addressToSend.setText(address);
    }

    private BigDecimal buildTotalFee(long totalFeeInSatoshis) {
        BigDecimal totalFeeInBtc = valueOf(totalFeeInSatoshis / 100000000.0).setScale(8, FLOOR);
        totalFee.setText(totalFeeInBtc.toPlainString().concat(" BTC"));
        return totalFeeInBtc;
    }

    private void buildFeeRate(long feeRateInSatoshisPerByte) {
        feeRate.setText(
            valueOf((feeRateInSatoshisPerByte / 100000000.0) * 1024)
                .setScale(8, FLOOR)
                .toPlainString()
                .concat(" BTC/kB")
        );
    }

    private void buildTotal(BigDecimal totFee, String amountToSend) {
        total.setText(new BigDecimal(amountToSend).add(totFee).toPlainString().concat(" BTC"));
    }

    public void buildTransactionInformation(String amountToSend, String addressToSend, WallyTransaction transaction) {
        buildAmountToSend(amountToSend);
        buildAddressToSend(addressToSend);
        BigDecimal totFee = buildTotalFee(transaction.getTotalFeeInSatoshis());
        buildFeeRate(transaction.getFeeRateInSatoshisPerByte());
        buildTotal(totFee, amountToSend);
    }

    public boolean passwordIsValid(String hashedPassword) {
        return authenticationService.checkPassword(sendTransactionPassword.getText(), hashedPassword);
    }
}
