package com.nikitiuk.javawebserverhttp.serverside.threadpool;

public class Runner {

    public static void main(String[] args) {
        ThreadPooledServer server = new ThreadPooledServer(8888);
        new Thread(server).start();

        try {
            Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Stopping Server");
        server.stop();
    }
}