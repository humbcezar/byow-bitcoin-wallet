package byow.bitcoinwallet.controllers;

import byow.bitcoinwallet.entities.NextNestedSegwitAddress;
import byow.bitcoinwallet.entities.NextReceivingAddress;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static byow.bitcoinwallet.utils.CopyUtil.copy;

@Lazy
@Component
public class ReceiveTabController extends Tab implements BaseController {

    private final Resource fxml;

    private final ApplicationContext context;

    private final NextReceivingAddress nextReceivingAddress;

    private final NextNestedSegwitAddress nextNestedSegwitAddress;

    @FXML
    public TextField receivingAddress;

    @FXML
    public TextField nestedReceivingAddress;

    @FXML
    public Button copyDefault;

    @FXML
    public Button copyNested;

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
        copyDefault.setOnAction(event -> copyDefaultAddress());
        copyNested.setOnAction(event -> copyNestedAddress());
    }

    private void copyDefaultAddress() {
        copy(receivingAddress.getText());
    }

    private void copyNestedAddress() {
        copy(nestedReceivingAddress.getText());
    }
}
