package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.wally.WallyTransaction;
import byow.bitcoinwallet.entities.wally.WallyTransactionInput;
import byow.bitcoinwallet.entities.wally.WallyTransactionOutput;
import byow.bitcoinwallet.services.AuthenticationService;
import byow.bitcoinwallet.utils.SatoshiUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

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
    public GridPane inputsGridPane;

    @FXML
    public GridPane outputsGridPane;
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
        buildInputs(transaction.getInputs());
        buildOutputs(transaction.getOutputs());
    }

    private void buildOutputs(List<WallyTransactionOutput> outputs) {
        IntStream.range(0, outputs.size())
            .forEach(i -> {
                Label label = new Label(String.join(", ", outputs.get(i).getAddress(), SatoshiUtils.satoshiToBtc(BigInteger.valueOf(outputs.get(i).getAmount())).toPlainString().concat(" BTC")));
                label.setPadding(new Insets(0,0,0,20));
                outputsGridPane.add(
                    label,
                    0,
                    i
                );
            });
    }

    private void buildInputs(List<WallyTransactionInput> inputs) {
        IntStream.range(0, inputs.size())
            .forEach(i -> {
                Label label = new Label(String.join(", ", inputs.get(i).getTxId(), String.valueOf(inputs.get(i).getVout())));
                label.setPadding(new Insets(0,0,0,20));
                inputsGridPane.add(
                    label,
                    0,
                    i
                );
            });
    }

    public boolean passwordIsValid(String hashedPassword) {
        return authenticationService.checkPassword(sendTransactionPassword.getText(), hashedPassword);
    }
}
