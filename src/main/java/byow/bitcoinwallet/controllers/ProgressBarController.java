package byow.bitcoinwallet.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Lazy
@Component
public class ProgressBarController extends ProgressBar implements BaseController {
    @FXML
    public ProgressBar progressBar;

    private final Resource fxml;

    private final ApplicationContext context;

    @Autowired
    public ProgressBarController(
            @Value("classpath:/fxml/progress_bar.fxml") Resource fxml,
            ApplicationContext context
    ) throws IOException {
        this.fxml = fxml;
        this.context = context;
        construct(this.fxml, this.context);
    }
}
