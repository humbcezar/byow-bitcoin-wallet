package byow.bitcoinwallet.entities;

import org.springframework.data.annotation.CreatedDate;
import javax.persistence.*;
import java.util.*;

import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "tx_id")
    private String txId;

    @Column(name = "created_at")
    @CreatedDate
    private Date createdAt;

    @ManyToMany(fetch = EAGER)
    private Set<Wallet> wallets = new HashSet<>();

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "transaction_id")
    private Set<TransactionInput> transactionInputs;

    @OneToMany(fetch = EAGER)
    @JoinColumn(name = "wallet_id")
    private Set<TransactionOutput> transactionOutputs;

    public Transaction() {
    }

    public Transaction(String txId, Date createdAt) {
        this.txId = txId;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getTxId() {
        return txId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void appendWallet(Wallet wallet) {
        wallets.add(wallet);
    }

    public Set<Wallet> getWallets() {
        return wallets;
    }

    public Set<TransactionInput> getTransactionInputs() {
        return transactionInputs;
    }

    public Set<TransactionOutput> getTransactionOutputs() {
        return transactionOutputs;
    }

    public void setTransactionInputs(Set<TransactionInput> transactionInputs) {
        this.transactionInputs = transactionInputs;
    }

    public void setTransactionOutputs(Set<TransactionOutput> transactionOutputs) {
        this.transactionOutputs = transactionOutputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return getTxId().equals(that.getTxId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTxId());
    }
}
