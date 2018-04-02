package bitcoin;

import java.nio.ByteBuffer;

/**
 * Created by lucky on 2017/12/18.
 */
public class Block {
    private long timestamp;
    /*private String prevBlockHash;
    private String hash;
    private String data;*/
    private byte[] hash;
    private byte[] prevBlockHash;
    private byte[] data;
    private int nonce;

    /*public Block(String prevBlockHash, String data) {
        this.timestamp = System.nanoTime();
        this.prevBlockHash = prevBlockHash;
        this.data = data;
        this.hash = Encrypt.SHA256(hashData());
    }*/

    public Block(String data, byte[] prevBlockHash) {
        this.timestamp = System.nanoTime();
        this.prevBlockHash = prevBlockHash;
        this.data = data.getBytes();
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    public void setPrevBlockHash(byte[] prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Block{" +
                "timestamp=" + timestamp +
                ", prevBlockHash='" + prevBlockHash + '\'' +
                ", hash='" + hash + '\'' +
                ", data='" + data + '\'' +
                '}';
    }

    public byte[] hashData() {
        ByteBuffer buffer = ByteBuffer.allocate(prevBlockHash.length + data.length + 8);
        buffer.put(prevBlockHash);
        buffer.put(data);
        buffer.putLong(timestamp);
        return buffer.array();
    }
}
