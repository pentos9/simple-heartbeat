package com.spacex.heartbeat.rpc;

import java.util.HashMap;
import java.util.Map;

public class HeartbeatEntity {
    private long time;
    private String nodeId;
    private String error;
    private Map<String, Object> info = new HashMap<>();
}
