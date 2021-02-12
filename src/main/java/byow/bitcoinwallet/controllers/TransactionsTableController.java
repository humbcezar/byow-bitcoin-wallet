package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.TransactionRow;
import byow.bitcoinwallet.services.CurrentTransactions;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Lazy
public class TransactionsTableController extends TableView<TransactionRow> implements BaseController {
    @FXML
    public TableView<TransactionRow> transactionsTable;

    @FXML
    public TableColumn<TransactionRow, String> columnTransaction;

    @FXML
    public TableColumn<TransactionRow, String> columnTransactionBalance;

    @FXML
    public TableColumn<TransactionRow, String> columnTransactionConfirmations;

    @FXML
    public TableColumn<TransactionRow, String> columnTransactionDate;

    private Resource fxml;

    private ApplicationContext context;

    private CurrentTransactions currentTransactions;

    @Autowired
    public TransactionsTableController(
        @Value("classpath:/fxml/transactions_table.fxml") Resource fxml,
        ApplicationContext context,
        CurrentTransactions currentTransactions
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.currentTransactions = currentTransactions;
        construct(this.fxml, this.context);
    }

    public void initialize() {
        transactionsTable.setItems(currentTransactions.get());
        columnTransaction.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        columnTransactionBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        columnTransactionConfirmations.setCellValueFactory(new PropertyValueFactory<>("confirmations"));
        columnTransactionDate.setCellValueFactory(new PropertyValueFactory<>("date"));
    }
}
