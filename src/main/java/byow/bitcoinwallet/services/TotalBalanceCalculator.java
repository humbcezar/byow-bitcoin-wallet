package byow.bitcoinwallet.services;

import byow.bitcoinwallet.entities.ReceivingAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Lazy
@Component
public class TotalBalanceCalculator {

    private BigDecimal unconfirmedBalance = BigDecimal.ZERO;

    private BigDecimal confirmedBalance = BigDecimal.ZERO;

    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Autowired
    private CurrentReceivingAddresses currentReceivingAddresses;

    public void calculate() {
        unconfirmedBalance = currentReceivingAddresses.getReceivingAddresses()
            .stream()
            .filter(receivingAddress -> receivingAddress.getConfirmations() == 0)
            .map(ReceivingAddress::getBigDecimalBalance)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

        confirmedBalance = currentReceivingAddresses.getReceivingAddresses()
            .stream()
            .filter(receivingAddress -> receivingAddress.getConfirmations() > 0)
            .map(ReceivingAddress::getBigDecimalBalance)
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
