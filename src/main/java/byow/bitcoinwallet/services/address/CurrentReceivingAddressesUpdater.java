package byow.bitcoinwallet.services.address;

import byow.bitcoinwallet.entities.ReceivingAddress;
import byow.bitcoinwallet.services.gui.CurrentReceivingAddresses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.min;
import static java.math.BigDecimal.ZERO;
import static javafx.application.Platform.runLater;

@Component
public class CurrentReceivingAddressesUpdater {
    @Autowired
    private CurrentReceivingAddresses currentReceivingAddresses;

    @Autowired
    private BitcoindRpcClient bitcoindRpcClient;

    public int updateReceivingAddresses(List<String> addressList, List<Unspent> utxos) {
        Map<String, List<ReceivingAddress>> collectedAddressMap = utxos.stream()
            .map(utxo -> new ReceivingAddress(utxo.amount(), utxo.confirmations(), utxo.address()))
            .collect(Collectors.groupingBy(ReceivingAddress::getAddress));
        addressList.stream()
            .filter(address -> isSpent(collectedAddressMap, address))
            .forEach(address -> collectedAddressMap.put(address, List.of(new ReceivingAddress(ZERO, -1, address))));

        collectedAddressMap.forEach(
            (address, addresses) ->
                addresses.stream()
                    .filter(receivingAddress -> currentReceivingAddresses.contains(receivingAddress.getAddress()))
                    .reduce(
                        (address1, address2) -> new ReceivingAddress(
                            address1.getBigDecimalBalance().add(address2.getBigDecimalBalance()),
                            min(address1.getConfirmations(), address2.getConfirmations()),
                            address1.getAddress()
                        )
                    ).ifPresent(receivingAddress -> runLater(() ->
                        currentReceivingAddresses.updateReceivingAddress(
                            receivingAddress.getAddress(),
                            receivingAddress.getBalance(),
                            receivingAddress.getConfirmations()
                        ))
                    )
        );
        return collectedAddressMap.size();
    }

    public void updateReceivingAddresses(List<Unspent> utxos) {
        updateReceivingAddresses(currentReceivingAddresses.getAddresses(), utxos);
    }

    private boolean isSpent(Map<String, List<ReceivingAddress>> collectedAddressMap, String address) {
        return !collectedAddressMap.containsKey(address) &&
                currentReceivingAddresses.contains(address)
                && currentReceivingAddresses.getReceivingAddress(address).getBigDecimalBalance().compareTo(ZERO) > 0;
    }
}
