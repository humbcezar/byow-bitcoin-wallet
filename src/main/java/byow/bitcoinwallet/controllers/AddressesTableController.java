package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.entities.TransactionRow;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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

import static byow.bitcoinwallet.utils.CopyUtil.copy;

@Component
@Lazy
public class AddressesTableController extends TableView<ReceivingAddress> implements BaseController {
    @FXML
    public TableView<ReceivingAddress> addressesTable;

    @FXML
    public TableColumn<ReceivingAddress, String> columnAddress;

    @FXML
    public TableColumn<ReceivingAddress, BigDecimal> columnBalance;

    @FXML
    public TableColumn<ReceivingAddress, Integer> columnConfirmations;

    private Resource fxml;

    private ApplicationContext context;

    private CurrentReceivingAddresses currentReceivingAddresses;

    @Autowired
    public AddressesTableController(
        @Value("classpath:/fxml/addresses_table.fxml") Resource fxml,
        ApplicationContext context,
        CurrentReceivingAddresses currentReceivingAddresses
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.currentReceivingAddresses = currentReceivingAddresses;
        construct(this.fxml, this.context);
    }

    public void initialize() {
        addressesTable.setItems(
            new FilteredList<>(
                currentReceivingAddresses.getReceivingAddresses(),
                receivingAddress -> receivingAddress.getBigDecimalBalance().compareTo(BigDecimal.ZERO) > 0
                    && receivingAddress.getConfirmations() > -1
            )
        );
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        columnBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        columnConfirmations.setCellValueFactory(new PropertyValueFactory<>("confirmations"));

        buildContextMenu();
    }


    private void buildContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyMenu = new MenuItem("Copy");
        copyMenu.setOnAction(event -> {
            ReceivingAddress item = addressesTable.getSelectionModel().getSelectedItem();
            copy(item.getAddress());
        });
        contextMenu.getItems().add(copyMenu);
        addressesTable.setContextMenu(contextMenu);
    }
}
