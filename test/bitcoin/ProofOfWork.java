package bitcoin;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by lucky on 2017/12/18.
 */
public class ProofOfWork {
    private Block block;
    private BigInteger target;

    public ProofOfWork(Block block, BigInteger target) {
        this.block = block;
        this.target = target;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public BigInteger getTarget() {
        return target;
    }

    public void setTarget(BigInteger target) {
        this.target = target;
    }

    public byte[] prepareData(Block block, int nonce, int targetBits) {
        byte[] data = block.hashData();
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 16);
        buffer.put(data);
        buffer.putLong(targetBits);
        buffer.putLong(nonce);
        return buffer.array();
    }
}
