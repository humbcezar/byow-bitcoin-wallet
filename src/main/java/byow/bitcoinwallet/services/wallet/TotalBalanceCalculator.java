package byow.bitcoinwallet.services.wallet;

import byow.bitcoinwallet.services.UtxosGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

import java.math.BigDecimal;
import java.util.List;

@Lazy
@Component
public class TotalBalanceCalculator {

    private BigDecimal unconfirmedBalance = BigDecimal.ZERO;

    private BigDecimal confirmedBalance = BigDecimal.ZERO;

    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Autowired
    private UtxosGetter utxosGetter;

    public void calculate() {
        List<Unspent> utxos = utxosGetter.getUtxos();

        unconfirmedBalance = utxos
            .stream()
            .filter(utxo -> utxo.confirmations() == 0)
            .map(Unspent::amount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        confirmedBalance = utxos
            .stream()
            .filter(utxo -> utxo.confirmations() > 0)
            .map(Unspent::amount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        totalBalance = unconfirmedBalance.add(confirmedBalance);
    }

    public BigDecimal getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public BigDecimal getConfirmedBalance() {
        return confirmedBalance;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }
}
