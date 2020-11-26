package byow.bitcoinwallet.factories;

import byow.bitcoinwallet.controllers.*;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SpringComponentBuilderFactory implements BuilderFactory {
    @Autowired
    private ApplicationContext context;

    private final Set<Class<? extends BaseController>> controllers = Set.of(
        ReceiveTabController.class,
        BalanceTableController.class,
        ProgressBarController.class,
        FooterController.class,
        TotalBalanceController.class,
        SendTabController.class
    );

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (controllers.contains(type)) {
            return (Builder<Object>) () -> context.getBean(type);
        }
        JavaFXBuilderFactory defaultFactory = new JavaFXBuilderFactory();
        return defaultFactory.getBuilder(type);
    }
}
