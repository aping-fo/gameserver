package zk;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2017/7/18.
 */
public class FightClient implements NodeCacheListener {
    private NodeCache masterNode;
    private NodeDiscovery<String> node;

    public static void main(String[] args) throws Exception {
        FightClient fc = new FightClient();
        fc.start();
    }

    public void start() throws Exception {
        ZKConnection connection = new ZKConnection("localhost:2181");
        connection.start();

        masterNode = new NodeCache(connection.getCuratorFramework(), "/servers3",false);
        ExecutorService masterChangeExec = Executors.newSingleThreadExecutor();
        masterNode.getListenable().addListener(this, masterChangeExec);
        masterNode.start(false);
        masterChangeExec.execute(() -> {
            ChildData data = masterNode.getCurrentData();
            if (data != null)
                System.out.println(new String(data.getData()));
        });


        /*NodeDiscovery.NodeListener<String> nodeListener = new NodeDiscovery.NodeListener<String>(){
            @Override
            public void onNodeAdded(String path, String node) {
                System.out.println("" + node);
            }

            @Override
            public void onNodeRemoved(String path, String node) {
                System.out.println("remove ==>"+node);
            }

            @Ovrride
            public void onNodeUpdated(String path, String node) {
                System.out.println(node);
            }
        };

        NodeDiscovery.NodeDataParser<String> PARSER = new NodeDiscovery.NodeDataParser<String>(){

            @Override
            public String parse(String path, byte[] nodeData){
                try{
                    return new String(nodeData);
                } catch (Throwable e){
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isValid(String path, byte[] nodeData){
                return true;
            }
        };
        node = new NodeDiscovery<>(connection.getCuratorFramework(),
                "/servers", PARSER,
                nodeListener, Executors.newSingleThreadScheduledExecutor());
        node.start();
        PersistentNode node1 = new PersistentNode(connection.getCuratorFramework(), CreateMode.EPHEMERAL, false, "/servers/" + ThreadLocalRandom.current().nextInt(111), "ss2".getBytes());
        node1.start();
        node1.waitForInitialCreate(1, TimeUnit.DAYS);*/
    }

    @Override
    public void nodeChanged() throws Exception {
        ChildData data = masterNode.getCurrentData();
        System.out.println(data);
        if (data != null)
            System.out.println("master change =>" + new String(data.getData()));
    }
}
