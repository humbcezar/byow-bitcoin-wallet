package byow.bitcoinwallet.guitests;

import byow.bitcoinwallet.factories.SpringComponentBuilderFactory;
import byow.bitcoinwallet.services.LoadNodeWallet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit5.ApplicationExtension;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;

@SpringBootTest
@ExtendWith(ApplicationExtension.class)
@ActiveProfiles("test")
abstract public class TestBase {
    @Value("classpath:/fxml/main_window.fxml")
    protected Resource fxml;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected SpringComponentBuilderFactory springComponentBuilderFactory;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Autowired
    private LoadNodeWallet loadNodeWallet;

    protected Stage stage;

    public void start (Stage stage) throws Exception {
        loadNodeWallet.createOrLoadNodeWallet();
        createBalanceIfNecessary();
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(fxml.getURL());
        fxmlLoader.setControllerFactory(context::getBean);
        fxmlLoader.setBuilderFactory(springComponentBuilderFactory);
        Parent root = fxmlLoader.load();
        stage.setTitle("BYOW Wallet");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void createBalanceIfNecessary() {
        BigDecimal balance = bitcoindRpcClient.getBalance();
        if (balance.compareTo(new BigDecimal(100)) <= 0) {
            String address = bitcoindRpcClient.getNewAddress();
            bitcoindRpcClient.generateToAddress(1000, address);
        }
    }
}
