package byow.bitcoinwallet;

import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.services.address.*;
import com.blockstream.libwally.Wally;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class ByowApplication {

    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
    }

    @Bean
    public Object wordList() {
        return Wally.bip39_get_wordlist(Languages.EN);
    }

    @Bean
    public ZContext zContext() {
        return new ZContext();
    }

    @Bean
    public Socket subscriber(ZContext zContext) {
        return zContext.createSocket(SocketType.SUB);
    }

    @Bean
    public ReentrantLock reentrantLock() {
        return new ReentrantLock();
    }

    @Bean
    @Primary
    public AddressSequentialGenerator defaultAddressSequentialGenerator(DefaultAddressGeneratorByXPubKey defaultAddressGeneratorByXPubKey) {
        return new AddressSequentialGenerator(defaultAddressGeneratorByXPubKey);
    }

    @Bean
    public AddressSequentialGenerator nestedSegwitAddressSequentialGenerator(NestedSegwitAddressGeneratorByXPubKey nestedSegwitAddressGeneratorByXPubKey) {
        return new AddressSequentialGenerator(nestedSegwitAddressGeneratorByXPubKey);
    }
}
