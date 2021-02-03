package at.tugraz.oop2.server;

import at.tugraz.oop2.InterpolationException;
import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;

import java.io.*;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ServerThread extends Thread {

    Socket socket;
    String path;
    List<Sensor> sensorList = new ArrayList<>();


    ServerThread(Socket socket, String path) {
        this.socket = socket;
        this.path = path;
    }


    @Override
    public void run() {
        super.run();

        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            Object msg = null;

            while (true) {
                synchronized (inputStream) {
                    msg = inputStream.readObject();

                }
                if (msg instanceof String && msg.equals("queryLS")) {
                    Logger.serverRequestLS();
                    System.out.println("Server request is sent!");

                    File file = new File(path);
                    querySensors(file);
                    outputStream.writeObject(new WrapperLsObject(sensorList));
                    outputStream.reset();
                    Logger.serverResponseLS(sensorList);
                    System.out.println("Server response:");

                    System.out.println("| ----------------------------------------------------------------------------------------------------------------------------------------------|");
                    System.out.println("|           Id           |          Type          |      Location      |         Lat         |          Lon          |         Metric           |");
                    System.out.println("| ----------------------------------------------------------------------------------------------------------------------------------------------|");

                    sensorList.forEach(sensor -> {
                        String line = String.format("|  %20s |  %20s |  %20s |  %20s |  %20s |  %20s |", String.valueOf(sensor.getId()),
                                sensor.getType(), sensor.getLocation(), String.valueOf(sensor.getLatitude()),
                                String.valueOf(sensor.getLongitude()), sensor.getMetric());
                        System.out.println(line);
                    });
                } else if (msg instanceof DataQueryParameters) {
                    DataQueryParameters parameters = (DataQueryParameters) msg;
                    Logger.serverRequestData(parameters);
                    System.out.println("Server request is sent!");

                    DataSeries dataSeries = queryData(parameters);

                    outputStream.writeObject(dataSeries);
                    outputStream.reset();

                    if (dataSeries.size() != 0) {
                        Logger.serverResponseData(parameters, dataSeries);
                        System.out.println("Server response:");

                        System.out.println("| ----------------------------------------------|");
                        System.out.println("|      Timestamp        |         Value         |");
                        System.out.println("| ----------------------------------------------|");

                        dataSeries.forEach((DataPoint datapoint) -> {
                            String line = String.format("|  %20s |  %20s | ", datapoint.getTime().toString(), String.valueOf(datapoint.getValue()));
                            System.out.println(line);
                        });
                        System.out.println("| ----------------------------------------------|");
                    } else {
                        Logger.err("Two or more missing DataPoints existing after performing operation for requested interval");
                        System.out.println("Two or more missing DataPoints existing after performing operation for requested interval");
                    }
                } else if (msg instanceof SOMQueryParameters) {
                    SOMQueryParameters somParameters = (SOMQueryParameters) msg;
                    System.out.println("Server request is sent!");

                    ArrayList<String> invalidSensorsOrMetrics = new ArrayList<String>();
                    // do all of the given sensors exist (if given by IDs)
                    // if the sensor exists, does it offer the given metric
                    // query sensors if data not present / empty
                    if (sensorList.size() == 0) {
                        File file = new File(path);
                        querySensors(file);
                    }
                    for (Integer receivedSensorId : somParameters.getSensorIds()) {
                        Sensor sensor = null;
                        for (Sensor s : sensorList) {
                            if (s.getId() == receivedSensorId) {
                                sensor = s;
                                break;
                            }
                        }
                        if (sensor == null) {
                            invalidSensorsOrMetrics.add("(invalid sensor ID) " + receivedSensorId.toString());
                        } else {
                            String sensorMetric = sensor.getMetric();
                            String paramMetric = somParameters.getMetric();
                            if (!sensorMetric.equals(paramMetric)) {
                                invalidSensorsOrMetrics.add("(invalid sensor/metric pair): (sensor) " + sensor.getId() + " - (metric) " + paramMetric);
                            } else {
                                // TODO: list valid sensors
                                ClusteringResult result = queryCluster(somParameters);
                                outputStream.writeObject(result);
                                outputStream.reset();
                            }
                        }

                    }

                    if (invalidSensorsOrMetrics.size() != 0) {
                        System.out.println("heloooooogg");
                        outputStream.writeObject(null);
                        outputStream.writeObject(invalidSensorsOrMetrics);
                        outputStream.reset();
                        return;
                    }

                }
            }
        } catch (IOException e) {
            Logger.info("Interrupted I/O operations -> you got IOException");
            System.err.println("Disconnected!");
        } catch (ClassNotFoundException e) {
            Logger.info("No definition for class with specified name found -> you got ClassNotFoundException");
            System.err.println("Class definition missing!");
        }
    }


    private void querySensors(File file) throws IOException {
        boolean is_not_directory = false;
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                querySensors(f);
            } else {
                is_not_directory = true;
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                boolean ignoreFirst = true;
                while ((line = br.readLine()) != null) {
                    if (ignoreFirst) {
                        br.readLine();
                        ignoreFirst = false;
                    } else {
                        String[] objects = line.split(";", -1);
                        int id = Integer.parseInt(objects[0]);
                        String type = objects[1];
                        String location = objects[2];
                        double lat = Double.parseDouble(objects[3]);
                        double lon = Double.parseDouble(objects[4]);
                        String p1 = "";
                        String p2 = "";
                        if (type.equals("SDS011")) {
                            p1 = "P1";
                            p2 = "P2";
                        } else if (type.equals("DHT22")) {
                            p1 = "temperature";
                            p2 = "humidity";
                        } else if (type.equals("BME280")) {
                            p1 = "temperature";
                            p2 = "humidity";
                        }

                        Sensor s1 = new Sensor(id, type, lat, lon, location, p1);
                        Sensor s2 = new Sensor(id, type, lat, lon, location, p2);
                        sensorList.add(s1);
                        sensorList.add(s2);
                        break;
                    }
                }
            }
            if (is_not_directory)
                break;
        }
    }


    private DataSeries queryData(DataQueryParameters parameters)
            throws IOException {
        File dataDir = new File(path);
        List<File> sensorFiles = getSensorFiles(dataDir, parameters.getSensorId());
        Sensor sensor = getSensorFromFile(sensorFiles.get(0), parameters);
        List<DataPoint> dataPoints = new ArrayList<>();
        for (File sensorCsvFile : sensorFiles) {
            dataPoints.addAll(getDataPointsFromFile(sensorCsvFile, parameters));
        }
        DataSeries series = new DataSeries(sensor, (int) parameters.getInterval(), parameters.getOperation());

        if (parameters.getOperation() == null) {
            series.addAll(dataPoints);
        } else {
            try {
                List<DataPoint> interpolatedPoints = this.interpolateDataPoints(dataPoints,
                        parameters.getFrom(), parameters.getTo(), parameters.getInterval(), parameters.getOperation());
                series.addAll(interpolatedPoints);
            } catch (InterpolationException e) {
                return series;
            }
        }
        return series;
    }


    private List<DataPoint> filterDataPointsByTimeInterval(
            List<DataPoint> dataPoints,
            LocalDateTime from, LocalDateTime to,
            boolean fromExclusive, boolean toExclusive) {
        List<DataPoint> filteredPoints = new ArrayList<>();
        for (DataPoint dataPoint : dataPoints) {
            if ((dataPoint.getTime().isAfter(from) || (dataPoint.getTime().isEqual(from) && !fromExclusive))
                    && (dataPoint.getTime().isBefore(to) || dataPoint.getTime().isEqual(to) && !toExclusive)) {
                filteredPoints.add(dataPoint);
            }
        }
        Collections.sort(filteredPoints);
        return filteredPoints;
    }


    private List<DataPoint> interpolateDataPoints(List<DataPoint> dataPoints,
                                                  LocalDateTime from, LocalDateTime to,
                                                  long interval, DataSeries.Operation operation)
            throws InterpolationException
    {
        List<DataPoint> result = new ArrayList<>();
        int prevMissingIdx = -1;
        LocalDateTime prevMissingTime = null;
        boolean prevMissing = false;

        for (LocalDateTime t = from; t.compareTo(to) < 0; t = t.plusSeconds(interval))
        {
            List<DataPoint> intervalDataPoints = this.filterDataPointsByTimeInterval(
                    dataPoints, t, t.plusSeconds(interval), false, false);

            Double interpolationValue = this.performOperationOnDataPoints(intervalDataPoints, operation);

            if (interpolationValue == null)
            {
                if (prevMissing)
                {
                    throw new InterpolationException("");
                }
                else
                {
                    result.add(null);
                    prevMissingIdx = (result.size() - 1);
                    prevMissing = true;
                    prevMissingTime = t;
                }
            }
            else
            {
                result.add(new DataPoint(t, interpolationValue));
                if (prevMissing) {
                    DataPoint first = result.get(result.size()-3);
                    DataPoint second = result.get(result.size()-1);
                    double avgValue = (first.getValue() + second.getValue()) / 2.0;
                    DataPoint interpolated = new DataPoint(prevMissingTime, avgValue);
                    result.set(prevMissingIdx, interpolated);
                    prevMissing = false;
                }
            }
        }

        return result;
    }


    private Double performOperationOnDataPoints(List<DataPoint> dataPoints, DataSeries.Operation operation) {
        if (dataPoints.isEmpty())
            return null;

        switch (operation) {
            case MAX:
                return dataPoints.stream().mapToDouble(DataPoint::getValue).max().getAsDouble();
            case MIN:
                return dataPoints.stream().mapToDouble(DataPoint::getValue).min().getAsDouble();
            case MEAN:
                return dataPoints.stream().mapToDouble(DataPoint::getValue).average().orElse(0);
            case MEDIAN:
                Collections.sort(dataPoints);
                if (dataPoints.size() % 2 == 0)
                    return (dataPoints.get(dataPoints.size() / 2).getValue() +
                            dataPoints.get(dataPoints.size() / 2 - 1).getValue()) / 2;
                else
                    return dataPoints.get(dataPoints.size() / 2).getValue();
            default:
                return null;
        }
    }

    private List<DataPoint> interpolateDataPoints(List<DataPoint> dataPoints,
                                                  LocalDateTime from, LocalDateTime to,
                                                  long interval, int length, DataSeries.Operation operation)
            throws InterpolationException
    {
        List<DataPoint> result = new ArrayList<>();
        int prevMissingIdx = -1;
        boolean prevMissing = false;
        LocalDateTime prevMissingTime = null;
        int currValueIdx = -1;

        for (LocalDateTime t = from; t.compareTo(to) < 0; t = t.plusSeconds(interval))
        {
            currValueIdx++;

            List<DataPoint> intervalDataPoints = this.filterDataPointsByTimeInterval(
                    dataPoints, t, t.plusSeconds(interval), false, false);

            Double interpolationValue = this.performOperationOnDataPoints(intervalDataPoints, operation);

            if (interpolationValue == null)
            {
                if (prevMissing && currValueIdx % length == 0)
                {
                    double missingValue = result.get(prevMissingIdx-1).getValue();
                    DataPoint missingPoint = new DataPoint(prevMissingTime, missingValue);
                    result.set(prevMissingIdx, missingPoint);
                }
                else if (prevMissing && currValueIdx % length != 0)
                {
                    throw new InterpolationException("");
                }
                result.add(null);
                prevMissing = true;
                prevMissingTime = t;
                prevMissingIdx = currValueIdx;
            }
            else
            {
                result.add(new DataPoint(t, interpolationValue));
                if (prevMissing && currValueIdx%length == 1)
                {
                    result.set(prevMissingIdx, result.get(currValueIdx));
                    prevMissing = false;
                }
                else if (prevMissing)
                {
                    DataPoint first = result.get(result.size()-3);
                    DataPoint second = result.get(result.size()-1);
                    double interpolatedValue = (first.getValue() + second.getValue()) / 2.0;
                    DataPoint interpolated = new DataPoint(prevMissingTime, interpolatedValue);
                    result.set(prevMissingIdx, interpolated);
                    prevMissing = false;
                }
            }
        }

        return result;
    }

    private List<File> getSensorFiles(File dataDir, int sensorId) {
        String sensorIdStr = String.valueOf(sensorId);
        List<File> sensorFiles = new ArrayList<>();
        for (File sensorDir : dataDir.listFiles()) {
            if (sensorDir.isDirectory() && sensorDir.getName().contains(sensorIdStr)) {
                File[] csvFiles = sensorDir.listFiles();
                for (File csvFile : csvFiles) {
                    if (csvFile.getName().contains(sensorId + ".csv")) {
                        sensorFiles.add(csvFile);
                    }
                }
            }
        }
        return sensorFiles;
    }


    private Sensor getSensorFromFile(File sensorCsvFile, DataQueryParameters parameters)
            throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(sensorCsvFile));
        br.readLine(); // read header line and don't store it anywhere
        String[] line = br.readLine().split(";", -1); // read just second line of the file
        int id = Integer.parseInt(line[0]);
        String type = line[1];
        String location = line[2];
        double latitude = Double.parseDouble(line[3]);
        double longitude = Double.parseDouble(line[4]);
        return new Sensor(id, type, latitude, longitude, location, parameters.getMetric());
    }


    private List<DataPoint> getDataPointsFromFile(File sensorCsvFile, DataQueryParameters parameters)
            throws IOException {

        List<DataPoint> dataPoints = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(sensorCsvFile));
        String line;
        boolean skipHeader = true;
        while ((line = br.readLine()) != null) {
            if (skipHeader) {
                line = br.readLine();
                skipHeader = false;
            }
            String[] objects = line.split(";", -1);
            LocalDateTime time = Util.stringToLocalDateTime(objects[5]);
            String type = objects[1];

            if (time.isEqual(parameters.getFrom()) || (time.isAfter(parameters.getFrom()) && time.isBefore(parameters.getTo()))) {
                int metricValueIdx = -1;
                switch (type.toUpperCase()) {
                    case "SDS011":
                        if (parameters.getMetric().equals("P1")) {
                            metricValueIdx = 6;
                        } else if (parameters.getMetric().equals("P2")) {
                            metricValueIdx = 9;
                        }
                        break;
                    case "DHT22":
                        if (parameters.getMetric().equals("temperature")) {
                            metricValueIdx = 6;
                        } else if (parameters.getMetric().equals("humidity")) {
                            metricValueIdx = 7;
                        }
                        break;
                    case "BME280":
                        if (parameters.getMetric().equals("temperature")) {
                            metricValueIdx = 9;
                        } else if (parameters.getMetric().equals("humidity")) {
                            metricValueIdx = 10;
                        }
                        break;
                }
                if (metricValueIdx != -1) {
                    double metricValue = Double.parseDouble(objects[metricValueIdx]);
                    DataPoint dataPoint = new DataPoint(time, metricValue);
                    dataPoints.add(dataPoint);
                }
            }
        }
        return dataPoints;
    }


    // TODO: 2nd assignment implementation
    // cluster 1503,1693 P1 2018-01-01 2018-01-05 1h mean 24 2 2 1 0.5 5 0xCAFE 5

    // cluster 1555,1111 P1 2018-01-01 2018-01-01 1h mean 24 2 2 1 0.5 10 0xCAFE 10
    // TODO: additional checks (3rd and 4th point from ass description)
    private ClusteringResult queryCluster(SOMQueryParameters parameters) throws IOException {
        List<DataSeries> inputData = this.getCurves(parameters);
        SOMHandler somHandler = new SOMHandler(
                inputData, parameters.getGridHeight(), parameters.getGridWidth(), parameters.getLength(),
                parameters.getLearningRate(), parameters.getUpdateRadius(), (int) parameters.getIterationsPerCurve(),
                parameters.getAmountOfIntermediateResults());
        somHandler.run();
        Map<Integer, List<ClusterDescriptor>> trainingClusters = somHandler.getTrainingProgressClusters();
        List<ClusterDescriptor> finalCluster = somHandler.getClusters();
        return new ClusteringResult(trainingClusters, finalCluster);
    }


    private List<DataSeries> getCurves(SOMQueryParameters parameters) throws IOException {
        List<DataSeries> inputData = new ArrayList<>();
        List<Integer> sensorIds = new ArrayList<>();
        if (parameters.getSensorIds().get(0) == 0) {
            File file = new File(path);
            querySensors(file);
            for (int i = 0; i < sensorList.size(); i++) {
                if (i % 2 == 0) {
                    sensorIds.add(sensorList.get(i).getId());
                }
            }
            for (int id : sensorIds) {
                DataQueryParameters queryParameters = new DataQueryParameters(id, parameters.getMetric(),
                        parameters.getFrom(), parameters.getTo(), parameters.getOperation(), parameters.getInterval());

                // query data
                try {
                    DataSeries queryResult = queryData(queryParameters);
                    if (queryResult.isEmpty()) {
                        throw new Exception();
                    }
                    // add partitions

                    inputData.addAll(this.partitionDataSeries(queryResult, parameters.getLength()));
                } catch (Exception e) {
                    continue;
                    //TODO WARNING sensor not found
                }
                //System.out.print("SENSOR ID "+ id);
            }
        } else {
            for (int sensorId : parameters.getSensorIds()) {
                // for each sensorId query data to get DataSeries
                DataQueryParameters queryParameters = new DataQueryParameters(sensorId, parameters.getMetric(),
                        parameters.getFrom(), parameters.getTo(), parameters.getOperation(), parameters.getInterval());
                try {
                    DataSeries queryResult = queryData(queryParameters);

                    if (queryResult.isEmpty()) {
                        throw new Exception();
                    }
                    // add partitions
                    inputData.addAll(this.partitionDataSeries(queryResult, parameters.getLength()));
                } catch (Exception e) {
                    continue;
                    //TODO WARNING sensor not found
                }
                //System.out.print("SENSOR ID "+ sensorId);
            }


        }


        return inputData;
    }

    private List<DataSeries> partitionDataSeries(DataSeries dataSeries, int batchSize) {
        List<DataSeries> partitioned = new ArrayList<>();
        List<List<DataPoint>> partitions = Util.partitionList(new ArrayList<>(dataSeries), batchSize);

        for (List<DataPoint> points : partitions) {
            DataSeries partition = new DataSeries(dataSeries.getSensor(), dataSeries.getInterval(),
                    dataSeries.getOperation());
            partition.addAll(points);
            partitioned.add(partition);
        }

        return partitioned;
    }


    public void checkIfPartionable(LocalDateTime from, LocalDateTime to, int length, long interval) throws IllegalArgumentException {
        long diff = Duration.between(from, to).toSeconds();
        long numberOfPoints = diff / interval;
        if (numberOfPoints % length != 0) {
            throw new IllegalArgumentException("Cannot divide into arrays of the same lenght");
        }
    }


    private void queryPlotCluster(SOMQueryParameters parameters) throws IOException {


    }

}
