package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.services.CurrentWalletManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Lazy
@Component
public class ReceiveTabController extends Tab {

    private Resource fxml;

    private ApplicationContext context;

    private CurrentWalletManager currentWalletManager;

    public TextField receivingAddress;

    @Autowired
    public ReceiveTabController(
            @Value("classpath:/fxml/receive_tab.fxml") Resource fxml,
            ApplicationContext context,
            CurrentWalletManager currentWalletManager
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.currentWalletManager = currentWalletManager;
        setText("Receive");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(this.fxml.getURL());
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        fxmlLoader.setControllerFactory(context::getBean);
        fxmlLoader.load();
    }

    public void initialize() {
        currentWalletManager.currentReceivingAddressProperty().addListener(
                (observable, oldValue, newValue) -> receivingAddress.setText(newValue)
        );
    }
}
