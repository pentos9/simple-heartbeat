package com.spacex.heartbeat.rpc;

import java.util.Map;

public class HeartbeatHandlerImpl implements HeartbeatHandler {

    public Cmder handle(HeartbeatEntity entity) {
        HeartbeatListener listener = HeartbeatListener.getInstance();
        if (!listener.checkNodeValid(entity.getNodeId())) {
            listener.registerNode(entity.getNodeId(), entity);
        }

        Cmder cmder = new Cmder();
        cmder.setNodeId(entity.getNodeId());

        //
        System.out.println("list all nodes:");
        Map<String, Object> nodes = listener.getNodes();
        for (Map.Entry<String, Object> entry : nodes.entrySet()) {
            System.out.println(String.format("%s:%s", entry.getKey(), entry.getValue()));
        }
        System.out.println("process and handle a heart beat!");
        return cmder;
    }
}
