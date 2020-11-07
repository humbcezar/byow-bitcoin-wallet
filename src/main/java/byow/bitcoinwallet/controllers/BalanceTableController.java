package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
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

    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    @Autowired
    public BalanceTableController(
            @Value("classpath:/fxml/balance_table.fxml") Resource fxml,
            @Autowired ApplicationContext context,
            @Autowired CurrentReceivingAddressesManager currentReceivingAddressesManager
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.currentReceivingAddressesManager = currentReceivingAddressesManager;
        construct(this.fxml, this.context);
    }

    public void initialize() {
        balanceTable.setItems(
            new FilteredList<>(
                currentReceivingAddressesManager.getReceivingAddresses(),
                receivingAddress -> new BigDecimal(receivingAddress.getBalance()).compareTo(BigDecimal.ZERO) > 0
                    && receivingAddress.getConfirmations() > -1
            )
        );
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        columnBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        columnConfirmations.setCellValueFactory(new PropertyValueFactory<>("confirmations"));
    }
}
