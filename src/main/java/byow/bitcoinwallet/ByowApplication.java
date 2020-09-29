package byow.bitcoinwallet;

import byow.bitcoinwallet.enums.Languages;
import com.blockstream.libwally.Wally;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ByowApplication {
    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
    }

    @Bean
    public Object wordList() {
        return Wally.bip39_get_wordlist(Languages.EN);
    }
}
