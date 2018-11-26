import java.util.*;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    final double p_graph;
    final double p_malicious;
    final double p_txDistribution;
    final int numRounds;

    private boolean[] followees;
    private int[] score;

    private Set<Transaction> pendingTransactions;
    private Set<Candidate> pendingCandidates;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
        this.pendingCandidates = new HashSet<Candidate>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
        this.score = new int[followees.length];
        for(int i = 0; i < followees.length ; i++) {
            if(followees[i] == true){
                this.score[i] = 100;
            } else {
                this.score[i] = 0;
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        return this.pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {

        for (Candidate candidate : candidates) {
            if (pendingTransactions.contains(candidate.tx))
                this.score[candidate.sender] += 1;
        }

        this.pendingCandidates.addAll(candidates);

        int nFollowees = 0;
        for(boolean follows: this.followees) {
            if (follows == true)
                nFollowees += 1;
        }

        int nMaliciousNodes = (int) Math.ceil(nFollowees * this.p_malicious);
        int nCompliantNodes = nFollowees - nMaliciousNodes;
        int validTxCount = (int) Math.floor(nCompliantNodes * this.p_txDistribution);
        int threashold = Math.max(nMaliciousNodes, validTxCount);
        threashold = Math.min(threashold, (int) Math.ceil(this.numRounds * 0.7));

        Set<Candidate> trustedCandidates = new HashSet<Candidate>();
        for (Candidate candidate : this.pendingCandidates) {
            if (this.score[candidate.sender] >= threashold) {
                this.score[candidate.sender] += 1;
                trustedCandidates.add(candidate);
                this.pendingTransactions.add(candidate.tx);
            }
        }
        this.pendingCandidates.removeAll(trustedCandidates);
    }
}
