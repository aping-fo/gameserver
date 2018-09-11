package zk;

import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2017/7/18.
 */
public class FightServer {
    private PersistentEphemeralNode selfMasterNode;

    public static void main(String[] args) throws Exception{
        FightServer fs = new FightServer();
        fs.start(ThreadLocalRandom.current().nextInt(10000));
        Thread.sleep(10000000l);
    }

    public void start(int i) throws Exception{
        ZKConnection connection = new ZKConnection("localhost:2181");
        connection.start();
        selfMasterNode = new PersistentEphemeralNode(
                connection.getCuratorFramework(),  PersistentEphemeralNode.Mode.EPHEMERAL, "/servers3",("hello" + i).getBytes());
        selfMasterNode.start();
        System.out.println("begin to create master " + i);
        selfMasterNode.waitForInitialCreate(1, TimeUnit.DAYS);
        System.out.println("create master finish " + i);

        Thread.sleep(100000);
        //selfMasterNode.close();
        //connection.getCuratorFramework().close();
    }
}
