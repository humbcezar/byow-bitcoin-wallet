package byow.bitcoinwallet.controllers;

import javafx.scene.control.Label;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Lazy
@Component
public class FooterController extends Label implements BaseController {
    private Resource fxml;

    private ApplicationContext context;

    @Autowired
    public FooterController(
        @Value("classpath:/fxml/footer.fxml") Resource fxml,
        ApplicationContext context
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        construct(this.fxml, this.context);
    }
}
