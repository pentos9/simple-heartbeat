package com.spacex.heartbeat.rpc.interfaces;

public class HelloServiceImpl implements HelloService {
    @Override
    public void hello(String name) {
        System.out.println("hello service,name:" + name);
    }
}
