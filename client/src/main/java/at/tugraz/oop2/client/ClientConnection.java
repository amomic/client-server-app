package at.tugraz.oop2.client;

import at.tugraz.oop2.data.DataQueryParameters;
import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.Sensor;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Used for managing the connection to the server and for sending requests.
 */
public final class ClientConnection implements AutoCloseable {
    private final LinkedBlockingQueue<ConnectionEventHandler> connectionClosedEventHandlers;
    private final LinkedBlockingQueue<ConnectionEventHandler> connectionOpenedEventHandlers;

    public ClientConnection() {
        connectionClosedEventHandlers = new LinkedBlockingQueue<>();
        connectionOpenedEventHandlers = new LinkedBlockingQueue<>();
    }

    public void connect(String url, int port) throws IOException {
        //TODO connect to server and call OpenedEventHandler
    }

    @Override
    public void close() {
        //TODO close connection and call ClosedEventHandler
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
        throw new NotImplementedException("Implement in Assignment 1");//TODO
    }

    public CompletableFuture<DataSeries> queryData(DataQueryParameters dataQueryParameters) {
        throw new UnsupportedOperationException("Implement in Assignment 1");//TODO
    }

    @FunctionalInterface
    public interface ConnectionEventHandler {
        void apply();
    }
}
