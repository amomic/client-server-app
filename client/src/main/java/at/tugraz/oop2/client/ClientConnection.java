package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.DataQueryParameters;
import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.Sensor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Used for managing the connection to the server and for sending requests.
 */
public final class ClientConnection implements AutoCloseable {
    private final LinkedBlockingQueue<ConnectionEventHandler> connectionClosedEventHandlers;
    private final LinkedBlockingQueue<ConnectionEventHandler> connectionOpenedEventHandlers;

    // added
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket clientSocket;

    public ClientConnection() {
        connectionClosedEventHandlers = new LinkedBlockingQueue<>();
        connectionOpenedEventHandlers = new LinkedBlockingQueue<>();
    }

    public void connect(String url, int port) throws IOException {
        //TODO connect to server and call OpenedEventHandler -> DONE
        this.clientSocket = new Socket(url, port);
        addConnectionOpenedListener(() -> Logger.info("Client connected."));

        outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
        inputStream = new ObjectInputStream(this.clientSocket.getInputStream());

    }

    @Override
    public void close() {
        //TODO close connection and call ClosedEventHandler -> DONE
        try {
            Logger.info("Closing socket and streams!");
            inputStream.close();
            outputStream.close();
            clientSocket.close();

            addConnectionClosedListener(() -> Logger.info("Client disconnected."));
        } catch (IOException ioException) {
            Logger.err("Socket not closed successfully!");
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

    // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public CompletableFuture<List<Sensor>> querySensors() throws IOException, ClassNotFoundException {
        CompletableFuture<List<Sensor>> sensors = new CompletableFuture<>();

        try {
            outputStream.writeObject("queryLS");
        } catch (IOException e) {
            e.printStackTrace();
        }

        @SuppressWarnings("unchecked")
        List<Sensor> sensorsList = (List<Sensor>) inputStream.readObject();
        sensors.complete(sensorsList);
        return sensors;
    }

    public CompletableFuture<DataSeries> queryData(DataQueryParameters dataQueryParameters) {
        throw new UnsupportedOperationException("Implement in Assignment 1");//TODO
    }

    @FunctionalInterface
    public interface ConnectionEventHandler {
        void apply();
    }

}