package byow.bitcoinwallet.entities;

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "seed",  unique = true, nullable = false)
    private String seed;

    @Column(name = "created_at")
    @CreatedDate
    private Date createdAt;

    @ManyToMany(mappedBy = "wallets", fetch = EAGER)
    private List<Transaction> transactions;

    public Wallet(String name, String seed) {
        this.name = name;
        this.seed = seed;
    }

    public Wallet() {}

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return name.equals(wallet.name) && seed.equals(wallet.seed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, seed);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
