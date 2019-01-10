// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory
// as it would cause a memory overflow.


// Code copied from: https://github.com/msilb/coursera-cryptocurrency/blob/master/assignment-3-blockchain/BlockChain.java
import java.util.ArrayList;
import java.util.HashMap;


public class BlockChain {
    private class Node {
        private Block block;
        private Node parent;
        public ArrayList<Node> children;
        public int height;
        private UTXOPool uPool;

        public Node(Block block, Node parent, UTXOPool uPool) {
            this.block = block;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.uPool = uPool;
            if (parent != null) {
                height = parent.height + 1;
                parent.children.add(this);
            } else {
                height = 1;
            }
        }

        public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(uPool);
        }
    }

    public static final int CUT_OFF_AGE = 10;

    private HashMap<ByteArrayWrapper, Node> blockChain;
    private Node maxHeightNode;
    private TransactionPool txPool;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        this.blockChain = new HashMap<>();
        UTXOPool utxoPool = new UTXOPool();
        this.addCoinbaseToUTXOPool(genesisBlock, utxoPool);
        Node genesisNode = new Node(genesisBlock, null, utxoPool);
        this.blockChain.put(BlockChain.wrap(genesisBlock.getHash()), genesisNode);
        this.txPool = new TransactionPool();
        this.maxHeightNode = genesisNode;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return this.maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return this.maxHeightNode.getUTXOPoolCopy();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     *
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     *
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null)
            return false;
        Node parentNode = blockChain.get(wrap(prevBlockHash));
        if (parentNode == null) {
            return false;
        }
        TxHandler handler = new TxHandler(parentNode.getUTXOPoolCopy());
        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);
        Transaction[] validTxs = handler.handleTxs(txs);
        if (validTxs.length != txs.length) {
            return false;
        }
        int proposedHeight = parentNode.height + 1;
        if (proposedHeight <= this.maxHeightNode.height - CUT_OFF_AGE) {
            return false;
        }
        UTXOPool utxoPool = handler.getUTXOPool();
        this.addCoinbaseToUTXOPool(block, utxoPool);
        Node node = new Node(block, parentNode, utxoPool);
        this.blockChain.put(BlockChain.wrap(block.getHash()), node);
        if (proposedHeight > this.maxHeightNode.height) {
            this.maxHeightNode = node;
        }
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
       this.txPool.addTransaction(tx);
    }

    private void addCoinbaseToUTXOPool(Block block, UTXOPool utxoPool) {
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output out = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, out);
        }
    }

    private static ByteArrayWrapper wrap(byte[] arr) {
        return new ByteArrayWrapper(arr);
    }
}