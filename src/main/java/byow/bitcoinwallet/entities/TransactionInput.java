package byow.bitcoinwallet.entities;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "transaction_input")
public class TransactionInput {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column
    private Long satoshis;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    public TransactionInput() {
    }

    public TransactionInput(Address address, Long satoshis) {
        this.address = address;
        this.satoshis = satoshis;
    }

    public Long getSatoshis() {
        return satoshis;
    }

    public Address getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionInput that = (TransactionInput) o;
        return getAddress().equals(that.getAddress()) && getSatoshis().equals(that.getSatoshis());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getSatoshis());
    }
}