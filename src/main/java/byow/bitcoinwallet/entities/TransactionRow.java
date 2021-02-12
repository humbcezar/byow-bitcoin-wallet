package byow.bitcoinwallet.entities;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;

@Component
@Lazy
public class TransactionRow {

    private StringProperty transactionId = new SimpleStringProperty();

    private StringProperty balance = new SimpleStringProperty();

    private BigDecimal bigDecimalBalance = ZERO;

    private IntegerProperty confirmations = new SimpleIntegerProperty();

    private StringProperty date = new SimpleStringProperty();

    public TransactionRow(
        String transactionId,
        String balance,
        Integer confirmations,
        String date
    ) {
        this.transactionId.set(transactionId);
        this.balance.set(balance);
        this.bigDecimalBalance = new BigDecimal(balance);
        this.confirmations.set(confirmations);
        this.date.set(date);
    }

    public String getTransactionId() {
        return transactionId.get();
    }

    public StringProperty transactionIdProperty() {
        return transactionId;
    }

    public String getBalance() {
        return balance.get();
    }

    public StringProperty balanceProperty() {
        return balance;
    }

    public Integer getConfirmations() {
        return confirmations.get();
    }

    public IntegerProperty confirmationsProperty() {
        return confirmations;
    }

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public BigDecimal getBigDecimalBalance() {
        return bigDecimalBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionRow that = (TransactionRow) o;
        return getTransactionId().equals(that.getTransactionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTransactionId());
    }
}
