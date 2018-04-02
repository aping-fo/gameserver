package zk;

import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2017/7/18.
 */
public class LeaderSelectorTest {

    public static void main(String[] args) throws Exception {
        try {
            ZKConnection connection = new ZKConnection("localhost:2181");
            connection.start();
            PersistentNode node1 = new PersistentNode(connection.getCuratorFramework(), CreateMode.EPHEMERAL,false,"/servers/1"+ ThreadLocalRandom.current().nextInt(111),("ss" + ThreadLocalRandom.current().nextInt(111)).getBytes());
            node1.start();
            node1.waitForInitialCreate(1, TimeUnit.DAYS);
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}