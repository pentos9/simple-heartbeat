package com.spacex.heartbeat.rpc;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HeartbeatListener {

    private ExecutorService executor = Executors.newFixedThreadPool(20);

    private final ConcurrentHashMap<String, Object> nodes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> nodeStatus = new ConcurrentHashMap<>();
    private long timeout = 10 * 1000;

    private int port = 8089;

    public ConcurrentHashMap<String, Long> getNodeStatus() {
        return nodeStatus;
    }

    private static class SingleHolder {
        private static final HeartbeatListener INSTANCE = new HeartbeatListener();
    }

    public static HeartbeatListener getInstance() {
        return SingleHolder.INSTANCE;
    }

    private HeartbeatListener() {
    }

    public ConcurrentHashMap<String, Object> getNodes() {
        return nodes;
    }

    public void registerNode(String nodeId, Object nodeInfo) {
        nodes.put(nodeId, nodeInfo);
        nodeStatus.put(nodeId, System.currentTimeMillis());
    }

    public void removeNode(String nodeId) {
        if (nodes.contains(nodeId)) {
            nodes.remove(nodeId);
        }
    }

    /**
     * is valid node
     *
     * @param key
     * @return
     */
    public boolean checkNodeValid(String key) {
        if (!nodes.contains(key) || !nodeStatus.contains(key)) {
            return false;
        }

        if ((System.currentTimeMillis() - nodeStatus.get(key)) > timeout) {
            return false;
        }
        return true;
    }

    /**
     * remove all invalid node
     */
    public void removeInValidNode() {
        Iterator<Map.Entry<String, Long>> it = nodeStatus.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if ((System.currentTimeMillis() - nodeStatus.get(e.getKey()) > timeout)) {
                nodes.remove(e.getKey());
            }
        }
    }
}
