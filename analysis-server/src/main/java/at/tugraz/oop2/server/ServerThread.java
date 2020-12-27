package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

                while(true) {
                    synchronized (inputStream) {
                        msg = inputStream.readObject();

                    }
                    if (msg instanceof String && msg.equals("queryLS")) {
                        Logger.serverRequestLS();
                        System.out.println("Server request is sent!");

                        File file = new File(path + "/sensors");
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
                       // CacheDataQueryParameters cacheDqp = new CacheDataQueryParameters(parameters);
                       // DataSeries dataSeries = AnalysisServer.cache.getCachedData(cacheDqp);


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

    private DataSeries queryData(DataQueryParameters parameters) throws IOException {
        File file = new File(path + "/sensors");

        // this will be overwritten by getData() so we use random values to avouid null warning
        Sensor sensor = new Sensor(parameters.getSensorId(), "", 2d,3d,"", parameters.getMetric());
        List<DataPoint> dataPoints = new ArrayList<>();
        Set<DataPoint> result = new TreeSet<>();
        List<Double> interpolationResultTracking = new ArrayList<>();

        getData(file, parameters, sensor, dataPoints);

        if(parameters.getOperation() == null) {
            result.addAll(dataPoints);
        } else {
            switch (parameters.getOperation()) {
                case NONE:
                    result.addAll(dataPoints);
                    break;

                case MIN:
                    int missingMeasureCounter = 0;
                    boolean hadInterpolation = false;
                    int interpolationIdx = 0;
                    Double interpolationPoint = null;

                    for(LocalDateTime start = parameters.getFrom();
                        start.compareTo(parameters.getTo()) < 0;
                        start = start.plusSeconds(parameters.getInterval())
                    ) {
                        final LocalDateTime currentStartTime = start;

                        TreeSet<DataPoint> dataPointsInInterval =  dataPoints.stream()
                                .filter((dataPoint) -> currentStartTime.compareTo(dataPoint.getTime()) <= 0 &&
                                        currentStartTime.plusSeconds(parameters.getInterval()).compareTo(dataPoint.getTime()) > 0)
                                .collect(Collectors.toCollection(TreeSet::new));

                        if(start.plusSeconds(parameters.getInterval()).compareTo(parameters.getTo()) > 0) {

                            if(dataPointsInInterval.size() == 0) {
                                if(missingMeasureCounter == 2) {
                                    result.clear();
                                    break;
                                }
                                interpolationPoint = (interpolationResultTracking.get(interpolationIdx - 1) + interpolationResultTracking.get(interpolationIdx - 2)) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                break;
                            } else {
                                double min = dataPointsInInterval.stream()
                                        .mapToDouble(DataPoint::getValue)
                                        .min()
                                        .getAsDouble();

                                DataPoint minData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == min)
                                        .findFirst().orElse(new DataPoint(currentStartTime, min));
                                interpolationResultTracking.add(min);

                                result.add(minData);
                                interpolationIdx++;
                                break;
                            }
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double min = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .min()
                                    .getAsDouble();

                            DataPoint minData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == min)
                                    .findFirst().orElse(new DataPoint(currentStartTime, min));
                            interpolationResultTracking.add(min);

                            if(hadInterpolation == true) {
                                interpolationPoint = (interpolationPoint + min) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                hadInterpolation = false;
                            }

                            result.add(minData);
                            interpolationIdx++;

                            missingMeasureCounter = 0;
                        } else {
                            hadInterpolation = true;
                            if(interpolationIdx == 0) {
                                interpolationPoint = 0d;
                            } else {
                                interpolationPoint = interpolationResultTracking.get(interpolationIdx - 1);
                            }

                            if(missingMeasureCounter == 2) {
                                result.clear();
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;

                case MAX:
                    missingMeasureCounter = 0;
                    hadInterpolation = false;
                    interpolationIdx = 0;
                    interpolationPoint = null;

                    for(LocalDateTime start = parameters.getFrom();
                        start.compareTo(parameters.getTo()) < 0;
                        start = start.plusSeconds(parameters.getInterval())
                    ) {
                        final LocalDateTime currentStartTime = start;

                        TreeSet<DataPoint> dataPointsInInterval =  dataPoints.stream()
                                .filter((dataPoint) -> currentStartTime.compareTo(dataPoint.getTime()) <= 0 &&
                                        currentStartTime.plusSeconds(parameters.getInterval()).compareTo(dataPoint.getTime()) > 0)
                                .collect(Collectors.toCollection(TreeSet::new));

                        if(start.plusSeconds(parameters.getInterval()).compareTo(parameters.getTo()) > 0) {

                            if(dataPointsInInterval.size() == 0) {
                                if(missingMeasureCounter == 2) {
                                    result.clear();
                                    break;
                                }
                                interpolationPoint = (interpolationResultTracking.get(interpolationIdx - 1) + interpolationResultTracking.get(interpolationIdx - 2)) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                break;
                            } else {
                                double max = dataPointsInInterval.stream()
                                        .mapToDouble(DataPoint::getValue)
                                        .max()
                                        .getAsDouble();

                                DataPoint maxData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == max)
                                        .findFirst().orElse(new DataPoint(currentStartTime, max));
                                interpolationResultTracking.add(max);

                                result.add(maxData);
                                interpolationIdx++;
                                break;
                            }
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double max = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .max()
                                    .getAsDouble();

                            DataPoint maxData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == max)
                                    .findFirst().orElse(new DataPoint(currentStartTime, max));
                            interpolationResultTracking.add(max);

                            if(hadInterpolation == true) {
                                interpolationPoint = (interpolationPoint + max) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                hadInterpolation = false;
                            }

                            result.add(maxData);
                            interpolationIdx++;

                            missingMeasureCounter = 0;
                        } else {
                            hadInterpolation = true;
                            if(interpolationIdx == 0) {
                                interpolationPoint = 0d;
                            } else {
                                interpolationPoint = interpolationResultTracking.get(interpolationIdx - 1);
                            }

                            if(missingMeasureCounter == 2) {
                                result.clear();
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;

                case MEAN:
                    missingMeasureCounter = 0;
                    hadInterpolation = false;
                    interpolationIdx = 0;
                    interpolationPoint = null;

                    for(LocalDateTime start = parameters.getFrom();
                        start.compareTo(parameters.getTo()) < 0;
                        start = start.plusSeconds(parameters.getInterval())
                    ) {
                        final LocalDateTime currentStartTime = start;

                        TreeSet<DataPoint> dataPointsInInterval =  dataPoints.stream()
                                .filter((dataPoint) -> currentStartTime.compareTo(dataPoint.getTime()) <= 0 &&
                                        currentStartTime.plusSeconds(parameters.getInterval()).compareTo(dataPoint.getTime()) > 0)
                                .collect(Collectors.toCollection(TreeSet::new));

                        if(start.plusSeconds(parameters.getInterval()).compareTo(parameters.getTo()) > 0) {

                            if(dataPointsInInterval.size() == 0) {
                                if(missingMeasureCounter == 2) {
                                    result.clear();
                                    break;
                                }
                                interpolationPoint = (interpolationResultTracking.get(interpolationIdx - 1) + interpolationResultTracking.get(interpolationIdx - 2)) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                break;
                            } else {
                                double avg = dataPointsInInterval.stream()
                                        .mapToDouble(DataPoint::getValue)
                                        .average()
                                        .getAsDouble();

                                DataPoint avgData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == avg)
                                        .findFirst().orElse(new DataPoint(currentStartTime, avg));
                                interpolationResultTracking.add(avg);

                                result.add(avgData);
                                interpolationIdx++;
                                break;
                            }
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double avg = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .average()
                                    .getAsDouble();

                            DataPoint avgData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == avg)
                                    .findFirst().orElse(new DataPoint(currentStartTime, avg));
                            interpolationResultTracking.add(avg);

                            if(hadInterpolation == true) {
                                interpolationPoint = (interpolationPoint + avg) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                hadInterpolation = false;
                            }

                            result.add(avgData);
                            interpolationIdx++;

                            missingMeasureCounter = 0;
                        } else {
                            hadInterpolation = true;
                            if(interpolationIdx == 0) {
                                interpolationPoint = 0d;
                            } else {
                                interpolationPoint = interpolationResultTracking.get(interpolationIdx - 1);
                            }

                            if(missingMeasureCounter == 2) {
                                result.clear();
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;

                case MEDIAN:
                    double median = 0;
                    missingMeasureCounter = 0;
                    hadInterpolation = false;
                    interpolationIdx = 0;
                    interpolationPoint = null;

                    for(LocalDateTime start = parameters.getFrom();
                        start.compareTo(parameters.getTo()) < 0;
                        start = start.plusSeconds(parameters.getInterval())
                    ) {
                        int sum = 0;

                        final LocalDateTime currentStartTime = start;

                        TreeSet<DataPoint> dataPointsInInterval =  dataPoints.stream()
                                .filter((dataPoint) -> currentStartTime.compareTo(dataPoint.getTime()) <= 0 &&
                                        currentStartTime.plusSeconds(parameters.getInterval()).compareTo(dataPoint.getTime()) > 0)
                                .collect(Collectors.toCollection(TreeSet::new));

                        if(start.plusSeconds(parameters.getInterval()).compareTo(parameters.getTo()) > 0) {

                            if(dataPointsInInterval.size() == 0) {
                                if(missingMeasureCounter == 2) {
                                    result.clear();
                                    break;
                                }
                                interpolationPoint = (interpolationResultTracking.get(interpolationIdx - 1) + interpolationResultTracking.get(interpolationIdx - 2)) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                break;
                            } else {
                                double[] medianList = dataPointsInInterval.stream()
                                        .mapToDouble(DataPoint::getValue)
                                        .sorted()
                                        .toArray();
                                for (double element : medianList) {
                                    sum++;
                                }
                                if (sum == 1) {
                                    median = medianList[0];
                                } else if (sum == 2) {
                                    median = (medianList[0] + medianList[1]) / 2;
                                } else if (sum > 2) {
                                    if (sum % 2 == 0) {
                                        int idx1 = sum / 2;
                                        int idx2 = sum / 2 - 1;
                                        median = (medianList[idx1] + medianList[idx2]) / 2;
                                    } else {
                                        int idx = sum / 2;
                                        median = medianList[idx];
                                    }
                                }

                                double finalMedian = median;
                                DataPoint medData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalMedian)
                                        .findFirst().orElse(new DataPoint(currentStartTime, median));
                                interpolationResultTracking.add(median);

                                result.add(medData);
                                interpolationIdx++;
                                break;
                            }
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double[] medianList = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .sorted()
                                    .toArray();
                            for(double element: medianList) {
                                sum++;
                            }

                            if(sum == 1) {
                                median = medianList[0];
                            } else if(sum == 2) {
                                median = (medianList[0] + medianList[1]) / 2;
                            } else if(sum > 2) {
                                if(sum % 2 == 0) {
                                    int idx1 = sum / 2;
                                    int idx2 = sum / 2 - 1;
                                    median = (medianList[idx1] + medianList[idx2]) / 2;;
                                } else {
                                    int idx = sum / 2;
                                    median = medianList[idx];
                                }
                            }

                            double finalMedian = median;
                            DataPoint medData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == finalMedian)
                                    .findFirst().orElse(new DataPoint(currentStartTime, median));
                            interpolationResultTracking.add(median);

                            if(hadInterpolation == true) {
                                interpolationPoint = (interpolationPoint + median) / 2;

                                double finalInterpolation = interpolationPoint;
                                DataPoint intData = dataPoints.stream()
                                        .filter(dataPoint -> dataPoint.getValue() == finalInterpolation)
                                        .findFirst().orElse(new DataPoint(currentStartTime, interpolationPoint));
                                result.add(intData);
                                hadInterpolation = false;
                            }

                            result.add(medData);
                            interpolationIdx++;

                            missingMeasureCounter = 0;
                        } else {
                            hadInterpolation = true;
                            if(interpolationIdx == 0) {
                                interpolationPoint = 0d;
                            } else {
                                interpolationPoint = interpolationResultTracking.get(interpolationIdx - 1);
                            }

                            if(missingMeasureCounter == 2) {
                                result.clear();
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;
            }
        }

        DataSeries series = new DataSeries(sensor, (int) parameters.getInterval(), parameters.getOperation());
        series.addAll(result);

        return series;
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
                        String[] objects = line.split(";",-1);
                        int id = Integer.parseInt(objects[0]);
                        String type = objects[1];
                        String location = objects[2];
                        double lat = Double.parseDouble(objects[3]);
                        double lon = Double.parseDouble(objects[4]);
                        String p1 = "";
                        String p2 = "";
                        if(type.equals("SDS011")) {
                            p1 = "P1";
                            p2 = "P2";
                        }else if (type.equals("DHT22"))
                        {
                            p1 = "temperature";
                            p2 = "humidity";
                        } else if (type.equals("BME280"))
                        {
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
            if(is_not_directory)
                break;
        }
    }

    private void getData(File file, DataQueryParameters parameters, Sensor sensor, List<DataPoint> dataPoints) throws IOException {
        String sensorId = String.valueOf(parameters.getSensorId());
        String metric = parameters.getMetric();
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory() && f.getName().contains(sensorId)) {
                File[] csvFiles = f.listFiles();
                for (File csvFile : csvFiles) {
                    if (csvFile.getName().contains(sensorId +".csv")) {
                        BufferedReader br = new BufferedReader(new FileReader(csvFile));
                        String line;
                        boolean ignoreFirst = true;
                        while ((line = br.readLine()) != null) {
                            if (ignoreFirst) {
                                line = br.readLine();
                                ignoreFirst = false;
                            }
                            String[] objects = line.split(";",-1);
                            int id = Integer.parseInt(objects[0]);
                            String type = objects[1];
                            String location = objects[2];
                            double lat = Double.parseDouble(objects[3]);
                            double lon = Double.parseDouble(objects[4]);
                            LocalDateTime time = Util.stringToLocalDateTime(objects[5]);
                            sensor = new Sensor(id,type,lat,lon,location, metric);

                            if (time.isEqual(parameters.getFrom()) || (time.isAfter(parameters.getFrom()) && time.isBefore(parameters.getTo()))) {
                                switch (type) {
                                    case "SDS011":
                                        if (metric.equals("P1")) {
                                            // index = 6
                                            double metricValue = Double.parseDouble(objects[6]);
                                            DataPoint dataPoint = new DataPoint(time, metricValue);
                                            dataPoints.add(dataPoint);
                                        } else if (metric.equals("P2")) {
                                            // index = 9
                                            double metricValue = Double.parseDouble(objects[9]);
                                            DataPoint dataPoint = new DataPoint(time, metricValue);
                                            dataPoints.add(dataPoint);
                                        }
                                        break;
                                    case "DHT22":
                                        if (metric.equals("temperature")) {
                                            // index = 6
                                            double metricValue = Double.parseDouble(objects[6]);
                                            DataPoint dataPoint = new DataPoint(time, metricValue);
                                            dataPoints.add(dataPoint);
                                        } else if (metric.equals("humidity")) {
                                            // index = 7
                                            double metricValue = Double.parseDouble(objects[7]);
                                            DataPoint dataPoint = new DataPoint(time, metricValue);
                                            dataPoints.add(dataPoint);
                                        }
                                        break;
                                    case "BME280":
                                        if (metric.equals("temperature")) {
                                            // index = 9
                                            double metricValue = Double.parseDouble(objects[9]);
                                            DataPoint dataPoint = new DataPoint(time, metricValue);
                                            dataPoints.add(dataPoint);
                                        } else if (metric.equals("humidity")) {
                                            // index = 10
                                            double metricValue = Double.parseDouble(objects[10]);
                                            DataPoint dataPoint = new DataPoint(time, metricValue);
                                            dataPoints.add(dataPoint);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: 2nd assignment implementation
    // FIXED: reading clustering data from file
    private void getClusteringData(File file, SOMQueryParameters parameters, Sensor sensor, List<DataPoint> dataPoints) throws IOException {

        String sensorId = String.valueOf(parameters.getSensorIds());
        String metric = parameters.getMetric();
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory() && f.getName().contains(sensorId)) {
                File[] csvFiles = f.listFiles();
                for (File csvFile : csvFiles) {
                    if (csvFile.getName().contains(sensorId +".csv")) {
                        BufferedReader br = new BufferedReader(new FileReader(csvFile));
                        String line;
                        boolean ignoreFirst = true;
                        while ((line = br.readLine()) != null) {
                            if (ignoreFirst) {
                                line = br.readLine();
                                ignoreFirst = false;
                            }
                            String[] objects = line.split(";",-1);

                            boolean isRequired = false;
                            Integer id = Integer.valueOf(objects[0]);

                            if(id == 0) {
                                isRequired = true;
                            } else {
                                for (Integer commandSensorId : parameters.getSensorIds()) {
                                    if (commandSensorId == id)
                                        isRequired = true;
                                }
                            }

                            if(isRequired) {
                                String type = objects[1];
                                String location = objects[2];
                                double lat = Double.parseDouble(objects[3]);
                                double lon = Double.parseDouble(objects[4]);
                                LocalDateTime time = Util.stringToLocalDateTime(objects[5]);
                                sensor = new Sensor(Integer.valueOf(sensorId), type, lat, lon, location, metric);

                                if (time.isEqual(parameters.getFrom()) || (time.isAfter(parameters.getFrom()) && time.isBefore(parameters.getTo()))) {
                                    switch (type) {
                                        case "SDS011":
                                            if (metric.equals("P1")) {
                                                // index = 6
                                                double metricValue = Double.parseDouble(objects[6]);
                                                DataPoint dataPoint = new DataPoint(time, metricValue);
                                                dataPoints.add(dataPoint);
                                            } else if (metric.equals("P2")) {
                                                // index = 9
                                                double metricValue = Double.parseDouble(objects[9]);
                                                DataPoint dataPoint = new DataPoint(time, metricValue);
                                                dataPoints.add(dataPoint);
                                            }
                                            break;
                                        case "DHT22":
                                            if (metric.equals("temperature")) {
                                                // index = 6
                                                double metricValue = Double.parseDouble(objects[6]);
                                                DataPoint dataPoint = new DataPoint(time, metricValue);
                                                dataPoints.add(dataPoint);
                                            } else if (metric.equals("humidity")) {
                                                // index = 7
                                                double metricValue = Double.parseDouble(objects[7]);
                                                DataPoint dataPoint = new DataPoint(time, metricValue);
                                                dataPoints.add(dataPoint);
                                            }
                                            break;
                                        case "BME280":
                                            if (metric.equals("temperature")) {
                                                // index = 9
                                                double metricValue = Double.parseDouble(objects[9]);
                                                DataPoint dataPoint = new DataPoint(time, metricValue);
                                                dataPoints.add(dataPoint);
                                            } else if (metric.equals("humidity")) {
                                                // index = 10
                                                double metricValue = Double.parseDouble(objects[10]);
                                                DataPoint dataPoint = new DataPoint(time, metricValue);
                                                dataPoints.add(dataPoint);
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: implement clustering with the help of getClusteringData command
    // Notes: first 7 args are covered by reading from csv file, other args are used as input for SOM algorithm
    //        get input from command line and use SOMQueryParameters for input of SOM algorithm
    //        write the SOM algorithm and do necessary calculations with the help of data mentioned above
    //        try setting weights to value between 0 and 1 and increase or decrease them with every iteration to get as near as possible to entered input
    //        return value

    private void queryCluster(SOMQueryParameters parameters) throws IOException {

    }
}