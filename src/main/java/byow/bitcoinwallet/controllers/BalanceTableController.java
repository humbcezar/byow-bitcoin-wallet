package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.CurrentAddressesManager;
import javafx.collections.transformation.FilteredList;
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
import java.math.BigDecimal;

@Component
@Lazy
public class BalanceTableController extends TableView<ReceivingAddress> implements BaseController {
    @FXML
    public TableView<ReceivingAddress> balanceTable;

    @FXML
    public TableColumn<ReceivingAddress, String> columnAddress;

    @FXML
    public TableColumn<ReceivingAddress, BigDecimal> columnBalance;

    @FXML
    public TableColumn<ReceivingAddress, Integer> columnConfirmations;

    private Resource fxml;

    private ApplicationContext context;

    private CurrentAddressesManager currentAddressesManager;

    @Autowired
    public BalanceTableController(
        @Value("classpath:/fxml/balance_table.fxml") Resource fxml,
        @Autowired ApplicationContext context,
        @Autowired CurrentAddressesManager currentAddressesManager
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.currentAddressesManager = currentAddressesManager;
        construct(this.fxml, this.context);
    }

    public void initialize() {
        balanceTable.setItems(
            new FilteredList<>(
                currentAddressesManager.getReceivingAddresses(),
                receivingAddress -> receivingAddress.getBigDecimalBalance().compareTo(BigDecimal.ZERO) > 0
                    && receivingAddress.getConfirmations() > -1
            )
        );
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        columnBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        columnConfirmations.setCellValueFactory(new PropertyValueFactory<>("confirmations"));
    }
}
