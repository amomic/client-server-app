package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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
    private HashMap<String, ClusteringResult> som_results_map;

    public ClientConnection() {
        connectionClosedEventHandlers = new LinkedBlockingQueue<>();
        connectionOpenedEventHandlers = new LinkedBlockingQueue<>();
        som_results_map = new HashMap<>();
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

    public CompletableFuture<DataSeries> queryLineChart(DataQueryParameters lineChartQueryParameters) throws IOException, ClassNotFoundException {
        CompletableFuture<DataSeries> dataSeriesCompletableFuture = new CompletableFuture<>();
        outputStream.writeObject(lineChartQueryParameters);

        DataSeries dataSeries = (DataSeries) inputStream.readObject();
        outputStream.reset();
        dataSeriesCompletableFuture.complete(dataSeries);
        return dataSeriesCompletableFuture;
    }

    public CompletableFuture<Picture> queryScatterPlot(ScatterPlotQueryParameters scatterPlotQueryParameters) throws IOException, ClassNotFoundException {
        CompletableFuture<Picture> pictureCompletableFuture = new CompletableFuture<>();
        outputStream.writeObject(scatterPlotQueryParameters);

        Picture picture = (Picture) inputStream.readObject();
        outputStream.reset();
        pictureCompletableFuture.complete(picture);
        return pictureCompletableFuture;
    }

    //TODO: implemetation of the 2nd assignment


    public CompletableFuture<List<ClusterDescriptor>> queryCluster(SOMQueryParameters somQueryParameters) throws Exception {
        CompletableFuture<List<ClusterDescriptor>> dataSeriesCompletableFuture = new CompletableFuture<>();

        ClusteringResult clusteringResult = null;
        outputStream.writeObject(somQueryParameters);
        String saveDirPath = "clusteringResults/";
        String saveDirId= "clusteringResults/"+ String.valueOf(somQueryParameters.getResultId()) ;
        //for(int i = 0; i < somQueryParameters.getAmountOfIntermediateResults(); i++) {
            //System.out.print("ANFANG FOR SCHLEIFE\n");
            clusteringResult = (ClusteringResult) inputStream.readObject();
            if(clusteringResult == null)
            {
                outputStream.reset();
                throw new Exception("Clustering error");
            }
            File directory = new File(saveDirPath);
            if (! directory.exists()) {
                directory.mkdirs();
            }
            File id_directory = new File(saveDirId);
            if (! id_directory.exists()) {
                id_directory.mkdirs();
            }
            else {
                throw new Exception("ResultID already used!");
            }
            //System.out.print("MITTE FOR SCHLEIFE\n");
            String saveJsonDir = saveDirPath + File.separator + String.valueOf(somQueryParameters.getResultId());
            saveClusteringResultsToJsonFile(somQueryParameters, clusteringResult, saveJsonDir);
            //List<ClusteringResult> entry = new ArrayList<>();
            //if(som_results_map.containsKey(Integer.toString(somQueryParameters.getResultId())))
            //    entry = som_results_map.get(Integer.toString(somQueryParameters.getResultId()));
            //entry.add(clusteringResult);
            som_results_map.put(Integer.toString(somQueryParameters.getResultId()), clusteringResult);
            outputStream.reset();
            //System.out.print("ENDEEE FOR SCHLEIFE\n");

        //System.out.print("AUßERHALB FOR SCHLEIFE\n");
        dataSeriesCompletableFuture.complete((List<ClusterDescriptor>) clusteringResult);
        //System.out.print("ENDDEEEE\n");
        return dataSeriesCompletableFuture;
    }


    public CompletableFuture<ClusteringResult> queryResult(int resultID) throws IOException, ClassNotFoundException, Exception
    {
        ClusteringResult entry = null;
        CompletableFuture<ClusteringResult> compFuture = new CompletableFuture<ClusteringResult>();
        Logger.clientRequestLS();
        if(som_results_map.containsKey(Integer.toString(resultID)))
            entry = som_results_map.get(Integer.toString(resultID));
        compFuture = CompletableFuture.completedFuture(entry);
        return compFuture;
    }





    private void saveClusteringResultsToJsonFile(SOMQueryParameters somQueryParameters, ClusteringResult result, String clusteringResultsDir) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        SimpleModule dataSeriesModule = new SimpleModule();
        dataSeriesModule.addSerializer(DataSeries.class, new DataSeriesJsonSerializer());
        mapper.registerModule(dataSeriesModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        // if directory doesn't exist create it
        File directory = new File(clusteringResultsDir);
        if (! directory.exists()){
            directory.mkdirs();
        }

        int i = 0;
        int add = (int) (somQueryParameters.getIterationsPerCurve() / somQueryParameters.getAmountOfIntermediateResults());
        // 10000/100 = 10

        // save training results first
        for (Map.Entry<Integer, List<ClusterDescriptor>> entry: result.getTrainingProgressClusters().entrySet())
        {
            File jsonFile = new File(clusteringResultsDir + File.separator + entry.getKey().toString() + ".json");
            jsonFile.createNewFile();
            writer.writeValue(jsonFile, entry.getValue());
            Logger.clientIntermediateResponse(somQueryParameters, i);
            i += add;
        }

        // save final result
        File jsonFile = new File(clusteringResultsDir + File.separator + "final.json");
        jsonFile.createNewFile();
        writer.writeValue(jsonFile, result.getFinalClusters());

    }

    @FunctionalInterface
    public interface ConnectionEventHandler {
        void apply();
    }

}

