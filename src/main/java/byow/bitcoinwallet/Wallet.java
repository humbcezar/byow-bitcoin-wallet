package byow.bitcoinwallet;

import javax.persistence.*;

@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "mnemonic_seed")
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
