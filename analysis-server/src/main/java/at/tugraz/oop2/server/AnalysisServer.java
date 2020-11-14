package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;

import java.net.*;
import java.io.*;

/**
 * This class will hold the implementation of your server and handle connecting and connected clients.
 */
public final class AnalysisServer {
    private final int serverPort;
    private final String dataPath;

    public AnalysisServer(int serverPort, String dataPath) {
        this.serverPort = serverPort;
        this.dataPath = dataPath;
    }

    private boolean waiting = true;
    public void run() {
        // FIXME Multithreading
        try {
            ServerSocket serverSocket = new ServerSocket(this.serverPort);
            while(waiting) {
                System.out.println("Waiting for the client request");
                // accept a connection
                Socket socket = serverSocket.accept();
                ProjectThread thread = new ProjectThread(socket, dataPath);
                thread.start();

            }
            System.out.println("Shutting down socket server");
            //close the ServerSocket object
            serverSocket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        // TODO Start here with a loop accepting new client connections.

    }
}
