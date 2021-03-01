package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.wallet.TotalBalanceCalculator;
import javafx.scene.control.Label;
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

    private static final String BALANCE_TEXT = "Total Balance: %s BTC (confirmed: %s, unconfirmed: %s)";

    private final TotalBalanceCalculator totalBalanceCalculator;

    @Autowired
    public TotalBalanceController(
        @Value("classpath:/fxml/total_balance.fxml") Resource fxml,
        ApplicationContext context,
        TotalBalanceCalculator totalBalanceCalculator
    ) throws IOException {
        this.totalBalanceCalculator = totalBalanceCalculator;
        construct(fxml, context);
    }

    public void clear() {
        setText("");
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
                totalBalanceCalculator.getTotalBalance().toString(),
                totalBalanceCalculator.getConfirmedBalance().toString(),
                totalBalanceCalculator.getUnconfirmedBalance().toString()
            )
        );
    }
}
