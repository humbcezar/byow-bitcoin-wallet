package byow.bitcoinwallet.entities;

import javafx.beans.property.*;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.util.Objects;

public class ReceivingAddress {

    private BigDecimalStringConverter bigDecimalStringConverter = new BigDecimalStringConverter();

    private StringProperty balance = new SimpleStringProperty();

    private IntegerProperty confirmations = new SimpleIntegerProperty();

    private StringProperty address = new SimpleStringProperty();

    public ReceivingAddress(BigDecimal balance, int confirmations, String address) {
        this.balance.setValue(bigDecimalStringConverter.toString(balance));
        this.confirmations.set(confirmations);
        this.address.setValue(address);
    }

    public String getBalance() {
        return balance.get();
    }

    public StringProperty balanceProperty() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance.set(balance);
    }

    public int getConfirmations() {
        return confirmations.get();
    }

    public IntegerProperty confirmationsProperty() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations.set(confirmations);
    }

    public String getAddress() {
        return address.get();
    }

    public StringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    @Override
    public String toString() {
        return "ReceivingAddress{" +
                "balance=" + balance +
                ", confirmations=" + confirmations +
                ", address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceivingAddress that = (ReceivingAddress) o;
        return getAddress().equals(that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }
}
