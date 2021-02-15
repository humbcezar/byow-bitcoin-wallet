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

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column
    private Long satoshis;

    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    public TransactionOutput(Address address, Long satoshis) {
        this.address = address;
        this.satoshis = satoshis;
    }

    public TransactionOutput() {
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
        TransactionOutput that = (TransactionOutput) o;
        return getAddress().equals(that.getAddress()) && getSatoshis().equals(that.getSatoshis());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getSatoshis());
    }
}
