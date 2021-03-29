package byow.bitcoinwallet.entities;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "x_pub")
public class XPub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "type", nullable = false)
    private String type;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    public XPub() {
    }

    public XPub(String key, String type, Wallet wallet) {
        this.key = key;
        this.type = type;
        this.wallet = wallet;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XPub xPub = (XPub) o;
        return getKey().equals(xPub.getKey()) && getType().equals(xPub.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey(), getType());
    }
}
