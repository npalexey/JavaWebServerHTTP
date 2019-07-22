package com.nikitiuk.javawebserverhttp.socketsubmit;

import java.net.ServerSocket;
public class MiniServerSocketExample {
    private static final int PORT = 7071;
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("MiniServer active " + PORT);
            while (true) {
                new ThreadSocket(server.accept());
            }
        } catch (Exception e) {
        }
    }
}