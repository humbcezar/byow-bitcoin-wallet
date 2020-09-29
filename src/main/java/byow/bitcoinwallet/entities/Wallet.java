package byow.bitcoinwallet.entities;

import javax.persistence.*;

@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    public Wallet(String name, String mnemonicSeed) {
        this.name = name;
        this.mnemonicSeed = mnemonicSeed;
    }

    public Wallet() {
    }

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "mnemonic_seed", unique = true, nullable = false)
    private String mnemonicSeed;

    public String getMnemonicSeed() {
        return mnemonicSeed;
    }

    public void setMnemonicSeed(String mnemonicSeed) {
        this.mnemonicSeed = mnemonicSeed;
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
}
