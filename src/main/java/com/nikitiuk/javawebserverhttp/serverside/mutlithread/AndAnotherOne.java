package com.nikitiuk.javawebserverhttp.serverside.mutlithread;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AndAnotherOne implements Runnable {

    private Socket csocket;

    private AndAnotherOne(Socket csocket) {
        this.csocket = csocket;
    }

    public static void main(String args[]) throws Exception {
        ServerSocket ssock = new ServerSocket(1234);
        System.out.println("Listening");

        while (true) {
            Socket sock = ssock.accept();
            System.out.println("Connected");
            new Thread(new AndAnotherOne(sock)).start();
        }
    }

    public void run() {
        try {
            PrintStream pstream = new PrintStream(csocket.getOutputStream());
            for (int i = 100; i >= 0; i--) {
                pstream.println(i + " bottles of beer on the wall");
            }
            pstream.close();
            csocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
