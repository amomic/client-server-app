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
    //public ServerThread serverThread;

    public AnalysisServer(int serverPort, String dataPath) {
        this.serverPort = serverPort;
        this.dataPath = dataPath;
    }

    private boolean waiting = true;

    public void run() {
        // TODO Start here with a loop accepting new client connections. -> DONE
        try {

            ServerSocket serverSocket = new ServerSocket(serverPort);
            Logger.info("Server: waiting for connection!");
            System.out.println("Server: waiting for connection!");

            while(waiting) {
                Socket socket = serverSocket.accept();

                Logger.info("Server: new client is connected!");
                System.out.println("Server: new client is connected!");

                new ServerThread(socket, dataPath).start();
            }
        } catch (IOException ioException) {
            Logger.info("Something went wrong with new client connection!");
            System.err.println("Disconnected!");
        }

    }
}