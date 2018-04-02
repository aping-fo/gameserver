package zk;

import static com.google.common.base.Preconditions.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * The {@code NodeDiscovery} class is used to watch a path in ZooKeeper. It will monitor which nodes
 * exist and fire node change events to subscribed instances of {@code NodeListener}. Users of this class should not
 * cache the results of discovery as subclasses can choose to change the set of available nodes based on some external
 * mechanism (ex. using bouncer).
 *
 * @param <T> The type that will be used to represent an active node.
 */
public class NodeDiscovery<T> implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscovery.class);

    /**
     * How long in milliseconds to wait between attempts to start.
     */
    private static final long WAIT_DURATION_IN_MILLIS = 100;

    private final ConcurrentMap<String, Optional<T>> _nodes;
    private final NodeListener<T> _listener;
    private final CuratorFramework _curator;
    private final PathChildrenCache _pathCache;
    private final NodeDataParser<T> _nodeDataParser;
    private final ScheduledExecutorService _executor;
    private boolean _closed;

    /**
     * Creates an instance of {@code ZooKeeperNodeDiscovery}.
     *
     * @param curator  Curator framework reference.
     * @param nodePath The path in ZooKeeper to watch.
     * @param parser   The strategy to convert from ZooKeeper {@code byte[]} to {@code T}.
     */
    public NodeDiscovery(CuratorFramework curator, String nodePath,
                         NodeDataParser<T> parser, NodeListener<T> lisener,
                         ScheduledExecutorService scheduledExec) {
        checkNotNull(curator);
        checkNotNull(nodePath);
        checkNotNull(parser);
        checkArgument(curator.getState() == CuratorFrameworkState.STARTED);
        checkArgument(!"".equals(nodePath));

        _nodes = Maps.newConcurrentMap();
        _listener = lisener;
        _curator = curator;
        _executor = scheduledExec;
        _pathCache = new PathChildrenCache(curator, nodePath, true, false,
                _executor);
        _nodeDataParser = parser;
        _closed = false;
    }

    /**
     * Start the NodeDiscovery.
     */
    public void start() {
        _pathCache.getListenable().addListener(new PathListener());
        startThenLoadData();
    }

    /**
     * Retrieve the available nodes.
     *
     * @return The available nodes.
     */
    public Map<String, T> getNodes() {
        return Maps.transformValues(Collections.unmodifiableMap(_nodes),
                new Function<Optional<T>, T>() {
                    @Override
                    public T apply(Optional<T> input) {
                        return (input != null) ? input.orNull() : null;
                    }
                });
    }

    /**
     * Returns true if the specified node is a member of the iterable returned by {@link #getNodes()}.
     *
     * @param node The node to test.
     * @return True if the specified node is a member of the iterable returned by {@link #getNodes()}.
     */
    public boolean contains(T node) {
        return _nodes.containsValue(Optional.fromNullable(node));
    }

    @Override
    public synchronized void close() throws IOException {
        if (!_closed) {
            _closed = true;
            _pathCache.close();
            _nodes.clear();
        }
    }

    @VisibleForTesting
    CuratorFramework getCurator() {
        return _curator;
    }

    /**
     * Start the underlying path cache and then populate the data for any nodes that existed prior to being created and
     * connected to ZooKeeper.
     * <p/>
     * This must be synchronized so async remove events aren't processed between start() and adding nodes.
     * Use synchronous start(true) instead of asynchronous start(false) so we can tell when it's done and the
     * node discovery set is usable.
     * <p/>
     * If there is a problem starting the path cache then we'll continue attempting to start it in a background thread
     * until the node discovery is closed.
     */
    private synchronized void startThenLoadData() {
        if (_closed) {
            return;
        }

        try {
            _pathCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Throwable t) {
            waitThenStartAgain();
            return;
        }

        loadExistingData();
    }

    /**
     * Wait a short period of time then try to start the path cache again.
     */
    private void waitThenStartAgain() {
        _executor.schedule(new Runnable() {
            @Override
            public void run() {
                startThenLoadData();
            }
        }, WAIT_DURATION_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * Loads all of the existing data from the underlying path cache.
     */
    private synchronized void loadExistingData() {
        for (ChildData childData : _pathCache.getCurrentData()) {
            if (_nodeDataParser.isValid(childData.getPath(),
                    childData.getData())) {
                addNode(childData.getPath(), parseChildData(childData));
            }
        }
    }

    private synchronized void addNode(String path, T node) {
        // synchronize the modification of _nodes and firing of events so listeners always receive events in the
        // order they occur.

        if (_nodes.put(path, Optional.fromNullable(node)) == null) {
            fireAddEvent(path, node);
        }
    }

    private synchronized void removeNode(String path, T node) {
        // synchronize the modification of _nodes and firing of events so listeners always receive events in the
        // order they occur.
        if (_nodes.remove(path) != null) {
            fireRemoveEvent(path, node);
        }
    }

    private synchronized void updateNode(String path, T node) {
        // synchronize the modification of _nodes and firing of events so listeners always receive events in the
        // order they occur.
        Optional<T> oldNode = _nodes.put(path, Optional.fromNullable(node));
        if (!Objects.equal(oldNode.orNull(), node)) {
            fireUpdateEvent(path, node);
        }
    }

    private void fireAddEvent(String path, T node) {
        _listener.onNodeAdded(path, node);
    }

    private void fireRemoveEvent(String path, T node) {
        _listener.onNodeRemoved(path, node);
    }

    private void fireUpdateEvent(String path, T node) {
        _listener.onNodeUpdated(path, node);
    }

    private T parseChildData(ChildData childData) {
        T value = null;
        try {
            value = _nodeDataParser.parse(childData.getPath(),
                    childData.getData());
        } catch (Exception e) {
            LOG.warn(
                    "NodeDataParser failed to parse ZooKeeper data. ZooKeeperPath: {}; Exception Message: {}",
                    childData.getPath(), e.getMessage());
            LOG.warn("Exception", e);
        }

        return value;
    }

    /**
     * A curator <code>PathChildrenCacheListener</code>
     */
    private final class PathListener implements PathChildrenCacheListener {
        @Override
        public void childEvent(CuratorFramework client,
                               PathChildrenCacheEvent event) throws Exception {
            switch (event.getType()) {
                case CHILD_ADDED:
                case CHILD_REMOVED:
                case CHILD_UPDATED:
                    break;

                default:
                    return;
            }

            String nodePath = null;
            ChildData nodeChildData = null;
            byte[] nodeByteData = null;
            if (event.getData() != null) {
                nodePath = event.getData().getPath();
                nodeChildData = event.getData();
            }

            if (nodeChildData != null) {
                nodeByteData = nodeChildData.getData();
            }

            if (!_nodeDataParser.isValid(nodePath, nodeByteData)) {
                return;
            }

            T nodeData = parseChildData(nodeChildData);

            switch (event.getType()) {
                case CHILD_ADDED:
                    addNode(nodePath, nodeData);
                    break;

                case CHILD_REMOVED:
                    removeNode(nodePath, nodeData);
                    break;

                case CHILD_UPDATED:
                    updateNode(nodePath, nodeData);
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * The {@code NodeDataParser} class is used to encapsulate the strategy that converts ZooKeeper node data into
     * a logical format for the user of {@code NodeDiscovery}.
     */
    public static interface NodeDataParser<T> {
        T parse(String path, byte[] nodeData);

        boolean isValid(String path, byte[] nodeData);
    }

    /**
     * Listener interface that is notified when nodes are added, removed, or updated.
     */
    public static interface NodeListener<T> {
        void onNodeAdded(String path, T node);

        void onNodeRemoved(String path, T node);

        void onNodeUpdated(String path, T node);
    }
}