package byow.bitcoinwallet.listeners;

import byow.bitcoinwallet.events.TransactionReceivedEvent;
import byow.bitcoinwallet.services.CurrentReceivingAddressesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.math.BigDecimal;

@Component
@Lazy
public class TransactionReceivedListener implements ApplicationListener<TransactionReceivedEvent> {
    @Autowired
    private CurrentReceivingAddressesManager currentReceivingAddressesManager;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    @Override
    public void onApplicationEvent(TransactionReceivedEvent event) {
        event.getRawTransaction()
                .vOut()
                .stream()
                .filter(vout -> vout.scriptPubKey().addresses() != null)
                .forEach(vout ->
                    vout.scriptPubKey()
                        .addresses()
                        .stream()
                        .filter(address -> currentReceivingAddressesManager.contains(address))
                        .map(address -> currentReceivingAddressesManager.get(address))
                        .forEach(receivingAddress -> {
                            bitcoindRpcClient.listUnspent(0, 6, receivingAddress.getAddress())
                                .stream()
                                .map(BitcoindRpcClient.TxOutput::amount)
                                .reduce(BigDecimal::add)
                                .ifPresent(sum -> receivingAddress.setBalance(sum.toString()));
                            int confirmations = event.getRawTransaction().confirmations() == null ? 0
                                    : event.getRawTransaction().confirmations();
                            receivingAddress.setConfirmations(confirmations);
                        })
                );
    }
}
