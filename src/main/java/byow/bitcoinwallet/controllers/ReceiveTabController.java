package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.NextNestedSegwitAddress;
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

    private NextNestedSegwitAddress nextNestedSegwitAddress;

    @FXML
    public TextField receivingAddress;

    @FXML
    public TextField nestedReceivingAddress;

    @Autowired
    public ReceiveTabController(
        @Value("classpath:/fxml/receive_tab.fxml") Resource fxml,
        ApplicationContext context,
        NextReceivingAddress nextReceivingAddress,
        NextNestedSegwitAddress nextNestedSegwitAddress
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        this.nextReceivingAddress = nextReceivingAddress;
        this.nextNestedSegwitAddress = nextNestedSegwitAddress;
        setText("Receive");
        construct(this.fxml, this.context);
    }

    public void initialize() {
        nextReceivingAddress.addListener(
            (observable, oldValue, newValue) -> receivingAddress.setText(newValue.getAddress())
        );
        nextNestedSegwitAddress.addListener(
            (observable, oldValue, newValue) -> nestedReceivingAddress.setText(newValue.getAddress())
        );
    }
}
