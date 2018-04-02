package bitcoin;

import javax.sound.midi.Soundbank;
import java.math.BigInteger;

/**
 * Created by lucky on 2017/12/18.
 */
public class ProofofworkModule {
    private static final int targetBits = 24;
    private static final long maxNonce = Long.MAX_VALUE;


    public ProofOfWork createProofOfWork(Block block) {
        BigInteger target = BigInteger.valueOf(1);
        target = target.shiftLeft(256 - targetBits);
        System.out.println(Encrypt.SHA256(target.toByteArray()));
        ProofOfWork proofOfWork = new ProofOfWork(block, target);
        return proofOfWork;
    }

    public Ret run(ProofOfWork proofOfWork) {
        byte[] hash = new byte[32];
        int nonce = 0;

        while (nonce < maxNonce) {
            byte[] data = proofOfWork.prepareData(proofOfWork.getBlock(), nonce, targetBits);
            hash = Encrypt.SHABytes(data);
            BigInteger hashInt = new BigInteger(hash);
            if (hashInt.compareTo(proofOfWork.getTarget()) == -1) {
                System.out.println("==" + Encrypt.SHA256(data));
                break;
            }
            nonce++;
        }
        return new Ret(nonce, hash);
    }


    static class Ret {
        public final int nonce;
        public final byte[] data;

        public Ret(int nonce, byte[] data) {
            this.nonce = nonce;
            this.data = data;
        }
    }
}
