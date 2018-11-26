import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class MaliciousNode implements Node {

    private Set<Transaction> pendingTransactions;

    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }

    public void setFollowees(boolean[] followees) {
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
        this.pendingTransactions.add(new Transaction(11));
        this.pendingTransactions.add(new Transaction(12));
    }

    public Set<Transaction> sendToFollowers() {
        return this.pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        return;
    }
}
