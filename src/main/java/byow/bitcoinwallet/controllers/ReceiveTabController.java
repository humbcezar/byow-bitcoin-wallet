package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.NextReceivingAddress;
import javafx.fxml.FXML;
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
public class ReceiveTabController extends Tab implements BaseController {

    private Resource fxml;

    private ApplicationContext context;

    private NextReceivingAddress nextReceivingAddress;

    @FXML
    public TextField receivingAddress;

    @Autowired
    public ReceiveTabController(
        @Value("classpath:/fxml/receive_tab.fxml") Resource fxml,
        ApplicationContext context,
        NextReceivingAddress nextReceivingAddress
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.nextReceivingAddress = nextReceivingAddress;
        setText("Receive");
        construct(this.fxml, this.context);
    }

    public void initialize() {
        nextReceivingAddress.addListener(
            (observable, oldValue, newValue) -> receivingAddress.setText(newValue.getAddress())
        );
    }
}
