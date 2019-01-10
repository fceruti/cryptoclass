import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;


public class TxHandler {
    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    public  getUTXOPool(){
        return this.utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool uniqueUtxos = new UTXOPool();
        double previousTxOutSum = 0;
        double currentTxOutSum = 0;

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(utxo);

            // (1) all outputs claimed by {@code tx} are in the current UTXO pool
            if (!utxoPool.contains(utxo)) {
                System.out.println("[ERROR CODE 1]");
                return false;
            }

            // (2) the signatures on each input of {@code tx} are valid
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), in.signature)){
                System.out.println("[ERROR CODE 2]");
                return false;
            }

            // (3) no UTXO is claimed multiple times by {@code tx}
            if (uniqueUtxos.contains(utxo)) {
                System.out.println("[ERROR CODE 4]");
                return false;
            }
            uniqueUtxos.addUTXO(utxo, output);
            previousTxOutSum += output.value;
        }

        for (Transaction.Output out : tx.getOutputs()) {
            // (4) all of {@code tx}s output values are non-negative, and
            if (out.value < 0) {
                System.out.println("[ERROR CODE 4]");
                return false;
            }
            currentTxOutSum += out.value;
        }

        // (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
        // values; and false otherwise.
        if (previousTxOutSum < currentTxOutSum) {
            System.out.println("[ERROR CODE 5] prev:" + previousTxOutSum + " current: " + currentTxOutSum);
        }
        return previousTxOutSum >= currentTxOutSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        final Set<Transaction> validTxs = Stream.of(possibleTxs)
            .filter(this::isValidTx)
            .peek(tx -> {
                for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    utxoPool.addUTXO(utxo, out);
                }
            })
            .collect(toSet());
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }

}