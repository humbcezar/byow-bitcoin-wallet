package byow.bitcoinwallet;

import byow.bitcoinwallet.enums.Languages;
import com.blockstream.libwally.Wally;
import javafx.application.Application;
import javafx.util.converter.BigDecimalStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.zeromq.ZContext;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import static wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient.DEFAULT_JSONRPC_REGTEST_URL;


@SpringBootApplication
public class ByowApplication {

    @Autowired
    public BitcoindRpcClient bitcoindRpcClient;

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
}
