package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.HashMap;


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
    private Map<Integer, CompletableFuture> responses = new HashMap<>();

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

        outputStream.writeObject("queryLS");

        WrapperLsObject wrapperLsObject = (WrapperLsObject) inputStream.readObject();
        outputStream.reset();
        sensors.complete(wrapperLsObject.getSensorList());
        return sensors;
    }

    public CompletableFuture<DataSeries> queryData(DataQueryParameters dataQueryParameters) throws IOException, ClassNotFoundException {
        CompletableFuture<DataSeries> seriesCompletableFuture = new CompletableFuture<>();
        outputStream.writeObject(dataQueryParameters);

        DataSeries dataSeries = (DataSeries) inputStream.readObject();
        outputStream.reset();
        seriesCompletableFuture.complete(dataSeries);
        return seriesCompletableFuture;
    }

    public CompletableFuture<Picture> queryLineChart(LineChartQueryParameters lineChartQueryParameters) throws IOException, ClassNotFoundException {
        CompletableFuture<Picture> pictureCompletableFuture = new CompletableFuture<>();
        outputStream.writeObject(lineChartQueryParameters);

        Picture picture = (Picture) inputStream.readObject();
        outputStream.reset();
        pictureCompletableFuture.complete(picture);
        return pictureCompletableFuture;
    }


    @FunctionalInterface
    public interface ConnectionEventHandler {
        void apply();
    }

}