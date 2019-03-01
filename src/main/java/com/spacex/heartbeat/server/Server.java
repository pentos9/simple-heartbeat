package com.spacex.heartbeat.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public interface ObjectAction {
        Object doAction(Object rev, Server server);
    }

    public static final class DefaultObjectAction implements ObjectAction {
        public Object doAction(Object rev, Server server) {
            System.out.println("Handle receive：" + rev);
            return rev;
        }
    }

    private int port;
    private volatile boolean running = false;
    private long receiveTimeDelay = 3000;
    private ConcurrentHashMap<Class, ObjectAction> actionMapping = new ConcurrentHashMap<Class, ObjectAction>();
    private Thread connWatchDog;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;
        connWatchDog = new Thread(new ConnWatchDog());
        connWatchDog.start();
    }

    public void stop() {
        if (running) {
            return;
        }

        if (connWatchDog != null) {
            connWatchDog.stop();
        }
    }

    public void addActionMap(Class<Object> cls, ObjectAction action) {
        actionMapping.put(cls, action);
    }

    class ConnWatchDog implements Runnable {
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port, 5);
                while (running) {
                    Socket socket = serverSocket.accept();
                    new Thread(new SocketAction(socket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Server.this.stop();
            }
        }
    }

    class SocketAction implements Runnable {

        private Socket socket;
        private boolean run = true;
        private long lastReceiveTime = System.currentTimeMillis();

        public SocketAction(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            while (running && run) {
                if (System.currentTimeMillis() - lastReceiveTime > receiveTimeDelay) {
                    overThis();
                } else {
                    try {
                        InputStream in = socket.getInputStream();
                        if (in.available() > 0) {
                            ObjectInputStream ois = new ObjectInputStream(in);
                            Object obj = ois.readObject();
                            lastReceiveTime = System.currentTimeMillis();
                            System.out.println("Receive：\t" + obj);
                            ObjectAction oa = actionMapping.get(obj.getClass());
                            oa = oa == null ? new DefaultObjectAction() : oa;
                            Object out = oa.doAction(obj, Server.this);
                            if (out != null) {
                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                oos.writeObject(out);
                                oos.flush();
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        overThis();
                    }
                }
            }
        }

        private void overThis() {
            if (run) run = false;
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("close：" + socket.getRemoteSocketAddress());

        }
    }


    public static void main(String[] args) {
        int port = 65432;
        Server server = new Server(port);
        server.start();
    }

}