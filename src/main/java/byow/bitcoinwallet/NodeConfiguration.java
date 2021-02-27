package byow.bitcoinwallet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import static com.blockstream.libwally.Wally.*;
import static wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient.*;

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

    @Bean(name = "networkVersion")
    public int defineNetworkVersion() {
        if (bitcoinEnvironment.equals("mainnet")) {
            return WALLY_NETWORK_BITCOIN_MAINNET;
        }
        return WALLY_NETWORK_BITCOIN_TESTNET;
    }

    @Bean(name = "nestedAddressVersion")
    public int defineAddressVersion() {
        if (bitcoinEnvironment.equals("mainnet")) {
            return WALLY_ADDRESS_VERSION_P2SH_MAINNET;
        }
        return WALLY_ADDRESS_VERSION_P2SH_TESTNET;
    }

    @Bean
    public BitcoindRpcClient bitcoindRpcClient() {
        if (bitcoinEnvironment.equals("mainnet")) {
            return new BitcoinJSONRPCClient(DEFAULT_JSONRPC_URL);
        }
        if (bitcoinEnvironment.equals("testnet")) {
            return new BitcoinJSONRPCClient(DEFAULT_JSONRPC_TESTNET_URL);
        }
        return new BitcoinJSONRPCClient(DEFAULT_JSONRPC_REGTEST_URL);
    }
}
