package com.spacex.heartbeat.rpc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cmder implements Serializable {
    private String nodeId;
    private String error;
    private Map<String, Object> info = new HashMap<>();

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }
}
