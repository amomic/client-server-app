package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.DataQueryParameters;
import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.Sensor;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;
import java.io.*;
import java.util.*;
/**
 * Used for managing the connection to the server and for sending requests.
 */
public final class ClientConnection implements AutoCloseable {
    private final LinkedBlockingQueue<ConnectionEventHandler> connectionClosedEventHandlers;
    private final LinkedBlockingQueue<ConnectionEventHandler> connectionOpenedEventHandlers;
    private Socket socket;

    public ClientConnection() {
        connectionClosedEventHandlers = new LinkedBlockingQueue<>();
        connectionOpenedEventHandlers = new LinkedBlockingQueue<>();
    }

    public void connect(String url, int port) throws IOException {
        //TODO connect to server and call OpenedEventHandle
        socket = new Socket(url,port);
        addConnectionOpenedListener(() -> System.out.println("Client disconnected."));
    }

    @Override
    public void close() {
        // close connection and call ClosedEventHandler
        try {
            connectionClosedEventHandlers.forEach(ConnectionEventHandler::apply);
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Registers a handler that will be called when the connection is opened.
     */
    public void addConnectionClosedListener(ConnectionEventHandler eventHandler) {
        connectionClosedEventHandlers.add(eventHandler);
    }

    /**
     * Registers a handler that will be called when the connection is closed either by
     * the client itself or by the server.
     */
    public void addConnectionOpenedListener(ConnectionEventHandler eventHandler) {
        connectionOpenedEventHandlers.add(eventHandler);
    }


    public CompletableFuture<List<Sensor>> querySensors() {
        CompletableFuture<List<Sensor>> sensors = new CompletableFuture<>();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject("queryLS");

            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            List<Sensor> sensorsList = (List<Sensor>) inputStream.readObject();
            sensors.complete(sensorsList);

            objectOutputStream.close();
            inputStream.close();

        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
        return sensors;
        // throw new NotImplementedException("Implement in Assignment 1");
    }

    public CompletableFuture<DataSeries> queryData(DataQueryParameters dataQueryParameters) {
        throw new UnsupportedOperationException("Implement in Assignment 1");//TODO
    }

    @FunctionalInterface
    public interface ConnectionEventHandler {
        void apply();
    }
}
