package blockchain.tasks;

import java.util.List;

public class HYI_Block {
    private int index;
    private long timestamp;
    private List<HYI_Transaction> transactions;
    private int nonce; // У методичці це називали proof
    private String previousHash;
    private String hash; // Зберігаємо власний хеш для зручності виводу

    public HYI_Block(int index, int nonce, String previousHash, List<HYI_Transaction> transactions) {
        this.index = index;
        this.nonce = nonce;
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = System.currentTimeMillis();
        // Хеш обчислюється зовні після знаходження Nonce, або можна тут, якщо nonce вже вірний
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getIndex() { return index; }
    public long getTimestamp() { return timestamp; }
    public List<HYI_Transaction> getTransactions() { return transactions; }
    public int getNonce() { return nonce; }
    public int getProof() { return nonce; }
    public String getPreviousHash() { return previousHash; }
    public String getHash() { return hash; }

    @Override
    public String toString() {
        return String.format("\n=== Block %d ===\n PrevHash: %s\n Hash: %s\n Nonce: %d\n Tx: %s",
                index, previousHash, hash, nonce, transactions.toString());
    }
}