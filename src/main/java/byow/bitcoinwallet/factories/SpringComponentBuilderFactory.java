package byow.bitcoinwallet.factories;

import byow.bitcoinwallet.controllers.ReceiveTabController;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringComponentBuilderFactory implements BuilderFactory {
    @Autowired
    private ApplicationContext context;

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type == ReceiveTabController.class) {
            return (Builder<Object>) () -> context.getBean(type);
        }
        JavaFXBuilderFactory defaultFactory = new JavaFXBuilderFactory();
        return defaultFactory.getBuilder(type);
    }
}
