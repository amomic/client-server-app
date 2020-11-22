package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;

import java.net.*;
import java.io.*;

/**
 * This class will hold the implementation of your server and handle connecting and connected clients.
 */
public final class AnalysisServer {
    public static Cache cache;
    private final int serverPort;
    private final String dataPath;
    public ServerThread serverThread;

    public AnalysisServer(int serverPort, String dataPath) {
        this.serverPort = serverPort;
        this.dataPath = dataPath;
    }

    private boolean waiting = true;

    public void run() {
        // TODO Start here with a loop accepting new client connections. -> DONE
        try {
            cache = new Cache(this);

            ServerSocket serverSocket = new ServerSocket(this.serverPort);
            Logger.info("Server: waiting for connection!");
            System.out.println("Server: waiting for connection!");

            while(waiting) {
                Socket socket = serverSocket.accept();

                Logger.info("Server: new client is connected!");
                System.out.println("Server: new client is connected!");

                serverThread = new ServerThread(socket, dataPath);
                serverThread.start();
            }
        } catch (Exception ioException) {
            Logger.info("Something went wrong with new client connection!");
            System.err.println("Disconnected!");
        }

    }
}