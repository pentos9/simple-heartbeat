package com.spacex.heartbeat.rpc;

import java.net.InetSocketAddress;
import java.util.UUID;

public class HeartbeatClient implements Runnable {
    private String serverIp = "127.0.0.1";
    private int serverPort = 8089;
    private String nodeId = UUID.randomUUID().toString();
    private boolean isRunning = true;

    private long lastHeartbeat;
    private long heartBeatInterval = 2 * 1000;


    @Override
    public void run() {
        while (isRunning) {
            HeartbeatHandler heartbeatHandler = RPClient.getRemoteProxyObj(HeartbeatHandler.class, new InetSocketAddress(serverIp, serverPort));
            long startTime = System.currentTimeMillis();
            if (startTime - lastHeartbeat > heartBeatInterval) {
                System.out.println("doSend a heart beat now!");
                lastHeartbeat = startTime;
                HeartbeatEntity heartbeatEntity = new HeartbeatEntity();
                heartbeatEntity.setTime(startTime);
                heartbeatEntity.setNodeId(nodeId);

                Cmder cmder = heartbeatHandler.handle(heartbeatEntity);
                if (!processCommand(cmder)) {
                    continue;
                }
            }
        }
    }

    private boolean processCommand(Cmder cmder) {
        return true;
    }
}
