package blockchain.tasks;

public class HYI_Transaction {
    private String sender;
    private String recipient;
    private int amount;

    public HYI_Transaction(String sender, String recipient, int amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public int getAmount() { return amount; }

    @Override
    public String toString() {
        return sender + " -> " + recipient + ": " + amount;
    }
}