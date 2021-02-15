package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.wallet.TotalBalanceCalculator;
import javafx.scene.control.Label;
import javafx.util.converter.BigDecimalStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

import static java.lang.String.format;

@Lazy
@Component
public class TotalBalanceController extends Label implements BaseController {
    private Resource fxml;

    private ApplicationContext context;

    private static String BALANCE_TEXT = "Total Balance: %s BTC (confirmed: %s, unconfirmed: %s)";

    private BigDecimalStringConverter bigDecimalStringConverter = new BigDecimalStringConverter();

    private TotalBalanceCalculator totalBalanceCalculator;

    @Autowired
    public TotalBalanceController(
        @Value("classpath:/fxml/total_balance.fxml") Resource fxml,
        ApplicationContext context,
        TotalBalanceCalculator totalBalanceCalculator
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.totalBalanceCalculator = totalBalanceCalculator;
        construct(this.fxml, this.context);
    }

    public void update() {
        totalBalanceCalculator.calculate();
        if(totalBalanceCalculator.getTotalBalance().equals(BigDecimal.ZERO)) {
            setText("");
            return;
        }
        setText(
            format(
                BALANCE_TEXT,
                bigDecimalStringConverter.toString(totalBalanceCalculator.getTotalBalance()),
                bigDecimalStringConverter.toString(totalBalanceCalculator.getConfirmedBalance()),
                bigDecimalStringConverter.toString(totalBalanceCalculator.getUnconfirmedBalance())
            )
        );
    }
}
