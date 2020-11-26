package byow.bitcoinwallet.entities;

import byow.bitcoinwallet.services.DerivationPath;
import javafx.beans.property.*;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.util.Objects;

public class ReceivingAddress {

    private BigDecimal bigDecimalBalance;

    private BigDecimalStringConverter bigDecimalStringConverter = new BigDecimalStringConverter();

    private StringProperty balance = new SimpleStringProperty();

    private IntegerProperty confirmations = new SimpleIntegerProperty();

    private StringProperty address = new SimpleStringProperty();

    private DerivationPath derivationPath;

    public ReceivingAddress(BigDecimal balance, int confirmations, String address) {
        this.bigDecimalBalance = balance;
        this.balance.setValue(bigDecimalStringConverter.toString(balance));
        this.confirmations.set(confirmations);
        this.address.setValue(address);
    }

    public ReceivingAddress(BigDecimal balance, int confirmations, String address, DerivationPath derivationPath) {
        this(balance, confirmations, address);
        this.derivationPath = derivationPath;
    }

    public synchronized String getBalance() {
        return balance.get();
    }

    public StringProperty balanceProperty() {
        return balance;
    }

    public synchronized void setBalance(String balance) {
        this.balance.set(balance);
        this.bigDecimalBalance = new BigDecimal(balance);
    }

    public synchronized int getConfirmations() {
        return confirmations.get();
    }

    public IntegerProperty confirmationsProperty() {
        return confirmations;
    }

    public synchronized void setConfirmations(int confirmations) {
        this.confirmations.set(confirmations);
    }

    public synchronized String getAddress() {
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

    public synchronized BigDecimal getBigDecimalBalance() {
        return bigDecimalBalance;
    }

    public DerivationPath getDerivationPath() {
        return derivationPath;
    }

    public void setDerivationPath(DerivationPath derivationPath) {
        this.derivationPath = derivationPath;
    }

}
