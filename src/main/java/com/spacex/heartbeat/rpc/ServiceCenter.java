package com.spacex.heartbeat.rpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceCenter {
    private ExecutorService executor = Executors.newFixedThreadPool(20);
    private ConcurrentHashMap<String, Class> serviceRegistry = new ConcurrentHashMap<>();

    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private int port = 8089;

    HeartbeatListener heartbeatListener;

    private static class SingleHolder {
        private static final ServiceCenter INSTANCE = new ServiceCenter();
    }

    private ServiceCenter() {
    }

    public static ServiceCenter getServiceCenter() {
        return SingleHolder.INSTANCE;
    }

    public void register(Class serviceInterface, Class impl) {
        System.out.println("register service:" + serviceInterface.getName());
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("start server");
        heartbeatListener = HeartbeatListener.getInstance();
        System.out.println("start listen heart beat");

        try {
            // listen client socket
            while (true) {
                executor.execute(new ServiceTask(serverSocket.accept()));
            }
        } finally {
            serverSocket.close();
        }

    }

    public void stop() {
        isRunning.set(false);
        executor.shutdown();
    }

    public ConcurrentHashMap<String, Class> getServiceRegistry() {
        return serviceRegistry;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private class ServiceTask implements Runnable {
        Socket client = null;

        public ServiceTask(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;

            try {
                input = new ObjectInputStream(client.getInputStream());

                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();

                Object[] arguments = (Object[]) input.readObject();

                Class serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(serviceName + " class not found!");
                }

                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(), arguments);
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
            } catch (Exception e) {
                System.err.println(e);
            } finally {
                if (input != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
