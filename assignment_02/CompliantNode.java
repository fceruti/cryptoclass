import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    final double p_graph;
    final double p_malicious;
    final double p_txDistribution;
    final double numRounds;

    private boolean[] followees;
    private int[] score;

    private Set<Transaction> pendingTransactions;
    private List<Candidate> candidates;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
        this.candidates = new ArrayList<Candidate>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.score = new int[followees.length];
        Arrays.fill(this.score, 0);
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        return this.pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            if (pendingTransactions.contains(candidate.tx)) {
                this.score[candidate.sender] += 1;
            } else {
                this.candidates.add(candidate);
            }
        }

        // TODO: add to probable
        List<Candidate> found = new ArrayList<Candidate>();
        for (Candidate candidate : this.candidates) {
            if (this.score[candidate.sender] > 10) {
                this.score[candidate.sender] += 1;
                this.pendingTransactions.add(candidate.tx);
                found.add(candidate);
            }
        }
        this.candidates.removeAll(found);
    }
}
