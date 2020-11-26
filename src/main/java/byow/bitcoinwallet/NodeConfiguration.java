package byow.bitcoinwallet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeConfiguration {

    private String bitcoinEnvironment;

    @Value("${bitcoin.network.environment}")
    public void setBitcoinEnvironment(String bitcoinEnvironment) {
        this.bitcoinEnvironment = bitcoinEnvironment;
    }

    @Bean(name = "addressPrefix")
    public String defineAddressPrefix() {
        if (bitcoinEnvironment.equals("mainnet")) {
            return "bc";
        }
        if (bitcoinEnvironment.equals("regtest")) {
            return "bcrt";
        }
        return "tb";
    }
}
