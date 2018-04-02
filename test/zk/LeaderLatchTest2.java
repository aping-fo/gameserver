package zk;

import com.game.util.RandomUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucky on 2017/7/18.
 */
public class LeaderLatchTest2 {
    private static final String PATH = "/demo/leader";

    public static void main(String[] args) {

        List<LeaderLatch> latchList = new ArrayList<>();
        List<CuratorFramework> clients = new ArrayList<>();
        try {
            //for (int i = 0; i < 10; i++) {
                CuratorFramework client = getClient();

                final LeaderLatch leaderLatch = new LeaderLatch(client, PATH, "client#" + RandomUtil.randInt(
                        1111
                ));
                leaderLatch.addListener(new LeaderLatchListener() {
                    @Override
                    public void isLeader() {
                        System.out.println(leaderLatch.getId() +  ":I am leader. I am doing jobs!");
                        try {
                            if(client.checkExists().forPath("/servers/1") == null) {
                                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/servers/1");
                            }
                            client.setData().forPath("/servers/1",leaderLatch.getId().getBytes());

                            //client.setData().forPath("/g4/fight",leaderLatch.getId().getBytes());
                            //client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/g4/fight",leaderLatch.getId().getBytes());
//                           PersistentEphemeralNode node = new PersistentEphemeralNode(client, PersistentEphemeralNode.Mode.EPHEMERAL,"/servers",leaderLatch.getId().getBytes());
//                           node.start();
//                           node.waitForInitialCreate(1, TimeUnit.DAYS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void notLeader() {
                        System.out.println(leaderLatch.getId() +  ":I am not leader. I will do nothing!");
                    }
                });
                latchList.add(leaderLatch);
                leaderLatch.start();
            //}
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for(CuratorFramework client : clients){
                CloseableUtils.closeQuietly(client);
            }

            for(LeaderLatch leaderLatch : latchList){
                CloseableUtils.closeQuietly(leaderLatch);
            }
        }
    }

    private static CuratorFramework getClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(6000)
                .connectionTimeoutMs(3000)
                .namespace("w")
                .build();
        client.start();
        return client;
    }
}
