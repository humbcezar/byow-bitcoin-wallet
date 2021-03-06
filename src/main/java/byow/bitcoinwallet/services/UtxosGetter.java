package byow.bitcoinwallet.services;

import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UtxosGetter {

    @Autowired
    private CurrentReceivingAddresses currentReceivingAddresses;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    public List<Unspent> getUtxos(List<String> addressList) {
        return bitcoindRpcClient.listUnspent(0, Integer.MAX_VALUE, addressList.toArray(new String[0]));
    }

    public List<Unspent> getUtxos() {
        return getUtxos(currentReceivingAddresses.getAddresses().stream().distinct().collect(Collectors.toList()));
    }
}
