package byow.bitcoinwallet.entities;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "transaction_output")
public class TransactionOutput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column
    private String address;

    @Column
    private Long satoshis;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    public TransactionOutput(String address, Long satoshis) {
        this.address = address;
        this.satoshis = satoshis;
    }

    public TransactionOutput() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionOutput that = (TransactionOutput) o;
        return address.equals(that.address) && satoshis.equals(that.satoshis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, satoshis);
    }

    public Long getSatoshis() {
        return satoshis;
    }
}
