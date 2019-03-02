package com.spacex.heartbeat.rpc;

public interface HeartbeatHandler {
    Cmder handle(HeartbeatEntity entity);
}
