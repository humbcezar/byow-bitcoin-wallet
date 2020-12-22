package byow.bitcoinwallet;

import byow.bitcoinwallet.enums.Languages;
import byow.bitcoinwallet.services.AddressSequentialGenerator;
import byow.bitcoinwallet.services.DefaultAddressGenerator;
import byow.bitcoinwallet.services.NestedSegwitAddressGenerator;
import com.blockstream.libwally.Wally;
import javafx.application.Application;
import javafx.util.converter.BigDecimalStringConverter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.util.concurrent.locks.ReentrantLock;

import static wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL;


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
    public BitcoindRpcClient bitcoindRpcClient() {
        return new BitcoinJSONRPCClient(DEFAULT_JSONRPC_REGTEST_URL);
    }

    @Bean
    public BigDecimalStringConverter bigDecimalStringConverter() {
        return new BigDecimalStringConverter();
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
    public AddressSequentialGenerator defaultAddressSequentialGenerator(DefaultAddressGenerator defaultAddressGenerator) {
        return new AddressSequentialGenerator(defaultAddressGenerator);
    }

    @Bean
    public AddressSequentialGenerator nestedSegwitAddressSequentialGenerator(NestedSegwitAddressGenerator nestedSegwitAddressGenerator) {
        return new AddressSequentialGenerator(nestedSegwitAddressGenerator);
    }
}
