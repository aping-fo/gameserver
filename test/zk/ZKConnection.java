package zk;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.io.Closeable;

/**
 * Created by lucky on 2017/7/18.
 */
public class ZKConnection implements Closeable {
    private final CuratorFramework curator;


    public ZKConnection(String zkConnectString) {
        RetryPolicy retryPolicy = new RetryNTimes(Integer.MAX_VALUE, 2000);
        this.curator = CuratorFrameworkFactory.builder()
                .connectString(zkConnectString).retryPolicy(retryPolicy)
                .namespace("w").build();
    }

    /**
     * 谁负责start的, 谁负责关
     */
    public void start() {
        curator.start();
    }

    @Override
    public void close() {
        curator.close();
    }

    public CuratorFramework getCuratorFramework() {
        return curator;
    }
}
