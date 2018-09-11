package zk;

import org.apache.curator.framework.recipes.nodes.PersistentNode;

import java.util.concurrent.Executors;


/**
 * Created by lucky on 2017/7/18.
 */
public class ManagerServer {
    public static void main(String[] args) throws Exception {

        NodeDiscovery.NodeDataParser<String> PARSER = new NodeDiscovery.NodeDataParser<String>() {

            @Override
            public String parse(String path, byte[] nodeData) {
                try {
                    return new String(nodeData);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public boolean isValid(String path, byte[] nodeData) {
                return true;
            }
        };

        NodeDiscovery.NodeListener<String> nodeListener = new NodeDiscovery.NodeListener<String>() {

            @Override
            public void onNodeAdded(String path, String node) {
                System.out.println(node);
            }

            @Override
            public void onNodeRemoved(String path, String node) {
            }

            @Override
            public void onNodeUpdated(String path, String node) {
                System.out.println(path + "**" + node);
            }
        };

        ZKConnection connection = new ZKConnection("localhost:2181");
        connection.start();

        NodeDiscovery<String> node = new NodeDiscovery<String>(connection.getCuratorFramework(),
                "/servers", PARSER,
                nodeListener, Executors.newScheduledThreadPool(1));

        node.start();
    }


}
