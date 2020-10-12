package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.CurrentWalletManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
public class BalanceTableController extends TableView<ReceivingAddress> {
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

    private CurrentWalletManager currentWalletManager;

    @Autowired
    public BalanceTableController(
            @Value("classpath:/fxml/balance_table.fxml") Resource fxml,
            @Autowired ApplicationContext context,
            @Autowired CurrentWalletManager currentWalletManager
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.currentWalletManager = currentWalletManager;
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(this.fxml.getURL());
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(context::getBean);
        fxmlLoader.load();
    }

    public void initialize() {
        balanceTable.setItems(currentWalletManager.getReceivingAddresses());
        columnAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        columnBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        columnConfirmations.setCellValueFactory(new PropertyValueFactory<>("confirmations"));
    }

}
