package com.spacex.heartbeat.client;

import com.spacex.heartbeat.KeepAlive;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Client {

    private String serverIp;
    private int port;
    private Socket socket;
    private boolean running = false;
    private long lastSendTime;

    private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class, ObjectAction>();

    public Client(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    public interface ObjectAction {
        void doAction(Object object, Client client);
    }

    public static final class DefaultObjectAction implements ObjectAction {
        public void doAction(Object object, Client client) {
            System.out.println("Client#DefaultObjectAction#doAction:\t" + object.toString());
        }
    }

    public void start() {
        if (running) {
            return;
        }

        try {
            socket = new Socket(serverIp, port);
            System.out.println(String.format("[Client]local address:%s,%s", serverIp, port));
            lastSendTime = System.currentTimeMillis();
            running = true;
            new Thread(new KeepAliveWatchDog()).start();//保持长连接的线程，每隔2秒项，server发一个保持连接的心跳消息
            new Thread(new ReceiveWatchDog()).start();//接受消息的线程，处理消息
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (running) {
            running = false;
        }
    }

    public void addAction(Class<Object> cls, ObjectAction objectAction) {
        actionMapping.put(cls, objectAction);
    }

    public void sendObject(Object object) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(object);
            System.out.println(String.format("Client#sendObject:%s", object));
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class KeepAliveWatchDog implements Runnable {
        private long checkDelay = 10;
        private long keepAliveDelay = 2000;

        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastSendTime > keepAliveDelay) {
                    Client.this.sendObject(new KeepAlive());
                    Client.this.stop();
                    lastSendTime = System.currentTimeMillis();
                } else {
                    try {
                        Thread.sleep(checkDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Client.this.stop();
                    }
                }


            }
        }
    }

    class ReceiveWatchDog implements Runnable {
        public void run() {
            while (running) {
                try {
                    InputStream inputStream = socket.getInputStream();
                    if (inputStream.available() > 0) {
                        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                        Object object = objectInputStream.readObject();
                        System.out.println("Client#ReceiveWatchDog#Handle:" + object.toString());
                        ObjectAction objectAction = actionMapping.get(object.getClass());
                        objectAction = objectAction == null ? new DefaultObjectAction() : objectAction;
                        objectAction.doAction(object, Client.this);
                    } else {
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Client.this.stop();
                }

            }
        }
    }

    public static void main(String[] args) {
        String serverIp = "127.0.0.1";
        int port = 65432;
        Client client = new Client(serverIp, port);
        client.start();
    }
}
