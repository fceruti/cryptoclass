// import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.math.BigInteger;
import java.security.*;


public class Main {
    public static void main(String args[]) throws  NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        // Crypto setup
        // You need the following JAR for RSA http://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
        // More information https://en.wikipedia.org/wiki/Bouncy_Castle_(cryptography)
        // Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        // Generating two key pairs, one for Scrooge and one for Alice
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey private_key_scrooge = pair.getPrivate();
        PublicKey public_key_scrooge = pair.getPublic();

        pair = keyGen.generateKeyPair();
        PrivateKey private_key_alice = pair.getPrivate();
        PublicKey public_key_alice = pair.getPublic();

        pair = keyGen.generateKeyPair();
        PrivateKey private_key_bob = pair.getPrivate();
        PublicKey public_key_bob = pair.getPublic();

        // START - ROOT TRANSACTION
        // Generating a root transaction tx out of thin air, so that Scrooge owns a coin of value 10
        // By thin air I mean that this tx will not be validated, I just need it to get a proper Transaction.Output
        // which I then can put in the UTXOPool, which will be passed to the TXHandler
        Transaction tx = new Transaction();
        tx.addOutput(10, public_key_scrooge);

        // that value has no meaning, but tx.getRawDataToSign(0) will access in.prevTxHash;
        byte[] initialHash = BigInteger.valueOf(1695609641).toByteArray();
        tx.addInput(initialHash, 0);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(private_key_scrooge);
        signature.update(tx.getRawDataToSign(0));
        byte[] sig = signature.sign();
        System.out.println(sig);
        tx.addSignature(sig, 0);
        tx.finalize();
        // END - ROOT TRANSACTION

        // The transaction output of the root transaction is unspent output
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx.getHash(), 0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));

        // remember that the utxoPool contains a single unspent Transaction.Output which is the coin from Scrooge
        TxHandler txHandler = new TxHandler(utxoPool);
        System.out.println("Is tx 1 valid?");
        System.out.println(" - " + txHandler.isValidTx(tx));

        txHandler.handleTxs(new Transaction[]{tx});

        // START - PROPER TRANSACTION
        Transaction tx2 = new Transaction();

        // the Transaction.Output of tx at position 0 has a value of 10
        tx2.addInput(tx.getHash(), 0);

        // I split the coin of value 10 into 3 coins and send all of them for simplicity to the same address (Alice)
        tx2.addOutput(5, public_key_alice);
        tx2.addOutput(3, public_key_alice);
        tx2.addOutput(2, public_key_alice);

        // There is only one (at position 0) Transaction.Input in tx2
        // and it contains the coin from Scrooge, therefore I have to sign with the private key from Scrooge
        signature.initSign(private_key_scrooge);
        signature.update(tx2.getRawDataToSign(0));
        sig = signature.sign();
        tx2.addSignature(sig, 0);
        tx2.finalize();

        System.out.println("Is tx 2 valid? ");
        System.out.println(" - " + txHandler.isValidTx(tx2));

        txHandler.handleTxs(new Transaction[]{tx2});

        // Two inputs one output
        Transaction tx3 = new Transaction();
        tx3.addInput(tx2.getHash(), 0);
        tx3.addInput(tx2.getHash(), 1);
        tx3.addOutput(8, public_key_bob);
        signature.initSign(private_key_alice);
        signature.update(tx3.getRawDataToSign(0));
        sig = signature.sign();
        tx3.addSignature(sig, 0);
        signature.initSign(private_key_alice);
        signature.update(tx3.getRawDataToSign(1));
        sig = signature.sign();
        tx3.addSignature(sig, 1);
        tx3.finalize();

        System.out.println("Is tx 3 valid? ");
        System.out.println(" - " + txHandler.isValidTx(tx3));

        txHandler.handleTxs(new Transaction[]{tx3});

        // Two output one input
        Transaction tx4 = new Transaction();
        tx4.addInput(tx3.getHash(), 0);
        tx4.addOutput(4, public_key_scrooge);
        tx4.addOutput(2, public_key_alice);

        signature.initSign(private_key_bob);
        signature.update(tx4.getRawDataToSign(0));
        sig = signature.sign();
        tx4.addSignature(sig, 0);
        tx4.finalize();

        System.out.println("Is tx 4 valid? ");
        System.out.println(" - " + txHandler.isValidTx(tx4));

        txHandler.handleTxs(new Transaction[]{tx4});

        // Multiple signatures
        Transaction tx5 = new Transaction();
        tx5.addInput(tx4.getHash(), 0);
        tx5.addInput(tx4.getHash(), 1);
        tx5.addOutput(4, public_key_bob);
        tx5.addOutput(2, public_key_bob);

        signature.initSign(private_key_scrooge);
        signature.update(tx5.getRawDataToSign(0));
        sig = signature.sign();
        tx5.addSignature(sig, 0);

        signature.initSign(private_key_alice);
        signature.update(tx5.getRawDataToSign(1));
        sig = signature.sign();
        tx5.addSignature(sig, 1);
        tx5.finalize();

        System.out.println("Is tx 5 valid? ");
        System.out.println(" - " + txHandler.isValidTx(tx5));

        txHandler.handleTxs(new Transaction[]{tx5});

        // Double spend
        Transaction tx6 = new Transaction();
        tx6.addInput(tx5.getHash(), 0);
        tx6.addOutput(3, public_key_alice);
        tx6.addOutput(3, public_key_scrooge);

        signature.initSign(private_key_bob);
        signature.update(tx6.getRawDataToSign(0));
        sig = signature.sign();
        tx6.addSignature(sig, 0);
        tx6.finalize();

        System.out.println("Is tx 6 valid? ");
        System.out.println(" - " + txHandler.isValidTx(tx6));

        txHandler.handleTxs(new Transaction[]{tx6});
    }
}