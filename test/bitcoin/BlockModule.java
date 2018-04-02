package bitcoin;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.List;

/**
 * Created by lucky on 2017/12/18.
 */
public class BlockModule {

    private final ProofofworkModule proofofworkModule;

    @Inject
    public BlockModule(ProofofworkModule proofofworkModule) {
        this.proofofworkModule = proofofworkModule;
    }

    /**
     * 创建区块
     *
     * @param data
     * @param prevBlockHash
     * @return
     */
    public Block createBlock(String data, byte[] prevBlockHash) {
        Block block = new Block(data, prevBlockHash);
        ProofOfWork proofOfWork = proofofworkModule.createProofOfWork(block);
        ProofofworkModule.Ret ret = proofofworkModule.run(proofOfWork);
        block.setNonce(ret.nonce);
        block.setHash(ret.data);
        return block;
    }

    /**
     * 生成创世块
     *
     * @return
     */
    public Block newGenesisBlock() {
        return createBlock("Genesis Block", new byte[]{});
    }

    private List<Block> blocks = Lists.newArrayList();

    public void addBlock(String data) {
        Block prevBlock = blocks.get(blocks.size() - 1);
        byte[] hash = prevBlock.getHash();

        Block newBlock = createBlock(data,hash);
        blocks.add(newBlock);
    }

    public void onStart() {
        if (blocks.isEmpty()) {
            Block genesisBlock = newGenesisBlock();
            blocks.add(genesisBlock);
        }
    }

    public void showAll() {
        blocks.forEach(System.out::println);
    }
}
