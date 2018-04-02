package zk;

import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by lucky on 2017/7/18.
 */
public class FightServer {
    private PersistentNode selfMasterNode;

    public static void main(String[] args) throws Exception{
        FightServer fs = new FightServer();
        fs.start(ThreadLocalRandom.current().nextInt(10000));
        Thread.sleep(10000000l);
    }

    public void start(int i) throws Exception{
        ZKConnection connection = new ZKConnection("localhost:2181");
        connection.start();
        selfMasterNode = new PersistentNode(
                connection.getCuratorFramework(), CreateMode.EPHEMERAL,false, "/servers/1",("hello" + i).getBytes());
        selfMasterNode.start();
        System.out.println("begin to create master " + i);
        selfMasterNode.waitForInitialCreate(1, TimeUnit.DAYS);
        System.out.println("create master finish " + i);

        //selfMasterNode.close();
        //connection.getCuratorFramework().close();
    }
}
