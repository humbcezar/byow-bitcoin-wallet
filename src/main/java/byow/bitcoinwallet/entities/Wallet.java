package byow.bitcoinwallet.entities;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;
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

    @Column(name = "seed", length = 500)
    private String seed;

    @Column(name = "created_at")
    @CreatedDate
    private Date createdAt;

    @Column(name = "last_imported_at")
    private Date lastImportedAt;

    @ManyToMany(mappedBy = "wallets", fetch = EAGER)
    private List<Transaction> transactions;

    @Column(name = "password")
    private String password;

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "wallet_id")
    private Set<XPub> xPubs;

    @OneToOne(fetch = EAGER)
    @JoinColumn(referencedColumnName = "id")
    private Wallet parent;

    public Wallet(String name, String seed) {
        this.name = name;
        this.seed = seed;
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10, new SecureRandom());
        this.password = bCryptPasswordEncoder.encode("");
    }

    public Wallet(String name, String seed, String password) {
        this.name = name;
        this.seed = seed;
        this.password = password;
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

    public Date getLastImportedAt() {
        return lastImportedAt;
    }

    public void setLastImportedAt(Date lastImportedAt) {
        this.lastImportedAt = lastImportedAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
        if (isWatchOnly()) {
            return parent.getTransactions();
        }
        return transactions;
    }

    public XPub getXPub(XPubTypes xPubType) {
        return xPubs.stream()
            .filter(xPub -> xPub.getType().equals(xPubType.toString()))
            .findFirst()
            .orElseThrow();
    }

    public Set<XPub> getxPubs() {
        return xPubs;
    }

    public Wallet getParent() {
        return parent;
    }

    public void setParent(Wallet parent) {
        this.parent = parent;
    }

    @Transient
    public boolean isWatchOnly() {
        return isNull(seed);
    }

}
