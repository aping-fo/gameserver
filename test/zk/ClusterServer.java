package zk;

import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2017/12/12.
 */
public class ClusterServer {
    public static void main(String[] args) throws Exception {
        ClusterServer fs = new ClusterServer();
        fs.start(ThreadLocalRandom.current().nextInt(10000));
        Thread.sleep(10000000l);
    }

    public void start(int i) throws Exception {
        ZKConnection connection = new ZKConnection("localhost:2181");
        connection.start();
        PersistentEphemeralNode selfMasterNode = new PersistentEphemeralNode(
                connection.getCuratorFramework(), PersistentEphemeralNode.Mode.EPHEMERAL,
                "/servers/1", (i + "").getBytes());
        System.out.println(i);
        selfMasterNode.start();
        selfMasterNode.waitForInitialCreate(1, TimeUnit.DAYS);
    }
}
