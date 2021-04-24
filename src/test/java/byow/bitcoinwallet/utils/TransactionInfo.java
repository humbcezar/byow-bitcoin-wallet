package byow.bitcoinwallet.utils;

import byow.bitcoinwallet.entities.TransactionRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionInfo {
    private final List<AddressRow> addressRow = new ArrayList<>();
    private String expectedNextAddress;
    private Map<String, TransactionRow> transactionRows;

    public void addAddressesRow(String address, String balance, int confirmations) {
        addressRow.add(new AddressRow(address, balance, confirmations));
    }

    public void setExpectedNextAddress(String expectedNextAddress) {
        this.expectedNextAddress = expectedNextAddress;
    }

    public void setTransactionRows(Map<String, TransactionRow> transactionRows) {
        this.transactionRows = transactionRows;
    }

    public List<AddressRow> getAddressRow() {
        return addressRow;
    }

    public String getExpectedNextAddress() {
        return expectedNextAddress;
    }

    public Map<String, TransactionRow> getTransactionRows() {
        return transactionRows;
    }

    public class AddressRow {
        private final String address;
        private final String balance;
        private final int confirmations;

        public AddressRow(String address, String balance, int confirmations) {
            this.address = address;
            this.balance = balance;
            this.confirmations = confirmations;
        }

        public String getAddress() {
            return address;
        }

        public String getBalance() {
            return balance;
        }

        public int getConfirmations() {
            return confirmations;
        }
    }
}
