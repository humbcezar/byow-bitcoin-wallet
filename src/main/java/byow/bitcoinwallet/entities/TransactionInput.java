package byow.bitcoinwallet.entities;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "transaction_input")
public class TransactionInput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column
    private String address;

    @Column
    private Long satoshis;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    public TransactionInput() {
    }

    public TransactionInput(String address, Long satoshis) {
        this.address = address;
        this.satoshis = satoshis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionInput that = (TransactionInput) o;
        return id == that.id && address.equals(that.address) && satoshis.equals(that.satoshis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, satoshis);
    }

    public Long getSatoshis() {
        return satoshis;
    }
}