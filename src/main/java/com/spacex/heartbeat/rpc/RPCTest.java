package com.spacex.heartbeat.rpc;

import com.spacex.heartbeat.rpc.interfaces.HelloService;
import com.spacex.heartbeat.rpc.interfaces.HelloServiceImpl;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RPCTest {
    public static void main(String[] args) {
        final int port = 8388;
        Thread workingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ServiceCenter serviceCenter = ServiceCenter.getServiceCenter();
                serviceCenter.register(HelloService.class, HelloServiceImpl.class);
                serviceCenter.register(HeartbeatHandler.class, HeartbeatHandlerImpl.class);
                try {
                    serviceCenter.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        workingThread.start();

        Thread client = new Thread(new HeartbeatClient());
        client.start();
    }
}