package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


// TODO: scatterplot, linechart, caching

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
            synchronized (inputStream) {
                msg = inputStream.readObject();
            }
            if (msg instanceof String && msg.equals("queryLS")) {
                Logger.serverRequestLS();
                File file = new File(path + "/sensors");
                querySensors(file);
                outputStream.writeObject(new WrapperLsObject(sensorList));
                outputStream.reset();
                Logger.serverResponseLS(sensorList);

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
                DataSeries dataSeries = queryData(parameters);
                outputStream.writeObject(dataSeries);
                outputStream.reset();
                Logger.serverResponseData(parameters, dataSeries);

                System.out.println("| ----------------------------------------------|");
                System.out.println("|      Timestamp        |         Value         |");
                System.out.println("| ----------------------------------------------|");

                dataSeries.forEach((DataPoint datapoint) -> {
                    String line = String.format("|  %20s |  %20s | ", datapoint.getTime().toString(), String.valueOf(datapoint.getValue()));
                    System.out.println(line);
                });
                System.out.println("| ----------------------------------------------|");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private DataSeries queryData(DataQueryParameters parameters) throws IOException {
        File file = new File(path + "/sensors");

        // this will be overwritten by getData() so we use random values to avouid null warning
        Sensor sensor = new Sensor(parameters.getSensorId(), "", 2d,3d,"", parameters.getMetric());
        List<DataPoint> dataPoints = new ArrayList<>();
        Set<DataPoint> result = new TreeSet<>();

        getData(file, parameters, sensor, dataPoints);


        // TODO data command: kad uzme from prvu liniju csv fajla nikad je ne include
        // TODO data command: interpolation
        // TODO data command: stop printing when error occurs -> check with 1s interval e.g.

        if(parameters.getOperation() == null) {
            result.addAll(dataPoints);
        } else {
            switch (parameters.getOperation()) {
                // TODO: check if none really should do this
                case NONE:
                    result.addAll(dataPoints);
                    break;

                case MIN:
                    int missingMeasureCounter = 0;

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
                            // TODO: ask if this is ok
                            double min = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .min()
                                    .getAsDouble();

                            DataPoint minData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == min)
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), min));
                            result.add(minData);
                            break;
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double min = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .min()
                                    .getAsDouble();

                            DataPoint minData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == min)
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), min));
                            result.add(minData);

                            missingMeasureCounter = 0;
                        } else {
                            if(missingMeasureCounter == 1) {
                                // TODO: implement interpolation

                            } else if(missingMeasureCounter == 2) {
                                // TODO: svaki put kada vrati error ne treba da printa tabele ni s client ni s server side
                                //result.clear();
                                //result.add(new DataPoint(LocalDateTime.now(), 1));
                                Logger.err("Two or more missing DataPoints in min operation for requested interval");
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;

                case MAX:
                    missingMeasureCounter = 0;

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
                            // TODO: ask if this is ok
                            double max = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .max()
                                    .getAsDouble();

                            DataPoint maxData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == max)
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), max));
                            result.add(maxData);
                            break;
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double max = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .max()
                                    .getAsDouble();

                            DataPoint maxData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == max)
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), max));
                            result.add(maxData);

                            missingMeasureCounter = 0;
                        } else {
                            if(missingMeasureCounter == 1) {
                                // TODO: implement interpolation

                            } else if(missingMeasureCounter == 2) {
                                // TODO: svaki put kada vrati error ne treba da printa tabele ni s client ni s server side
                                //result.clear();
                                // result.add(new DataPoint(LocalDateTime.now(), -1));
                                Logger.err("Two or more missing DataPoints in max operation for requested interval");
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;

                case MEAN:
                   missingMeasureCounter = 0;

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
                            // TODO: ask if this is ok
                            double avg = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .average()
                                    .getAsDouble();

                            DataPoint avgData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == avg)
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), avg));
                            result.add(avgData);
                            break;
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double avg = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .average()
                                    .getAsDouble();

                            DataPoint avgData = dataPoints.stream()
                                    .filter(dataPoint -> dataPoint.getValue() == avg)
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), avg));
                            result.add(avgData);

                            missingMeasureCounter = 0;
                        } else {
                            if(missingMeasureCounter == 1) {
                                // TODO: implement interpolation

                            } else if(missingMeasureCounter == 2) {
                                // TODO: svaki put kada vrati error ne treba da printa tabele ni s client ni s server side
                                //result.clear();
                                // result.add(new DataPoint(LocalDateTime.now(), -1));
                                Logger.err("Two or more missing DataPoints in mean operation for requested interval");
                                break;
                            }
                            missingMeasureCounter++;
                        }
                    }
                    break;

                case MEDIAN:
                    double median = 0;
                    missingMeasureCounter = 0;

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
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), median));
                            result.add(medData);
                            break;
                        }

                        if(!dataPointsInInterval.isEmpty()) {
                            double[] medianList = dataPointsInInterval.stream()
                                    .mapToDouble(DataPoint::getValue)
                                    .sorted()
                                    .toArray();
                            for(double element: medianList) {
                                sum++;
                            }
                            // FIXME: also weird stuff happening with a few first lines of csv file
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
                                    .findFirst().orElse(new DataPoint(LocalDateTime.now(), median));
                            result.add(medData);

                            missingMeasureCounter = 0;
                        } else {
                            if(missingMeasureCounter == 1) {
                                // TODO: implement interpolation

                            } else if(missingMeasureCounter == 2) {
                                // TODO: svaki put kada vrati error ne treba da printa tabele ni s client ni s server side
                                //result.clear();
                                // result.add(new DataPoint(LocalDateTime.now(), -1));
                                Logger.err("Two or more missing DataPoints in median operation for requested interval");
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
                                br.readLine();
                                ignoreFirst = false;
                            } else {
                                String[] objects = line.split(";",-1);
                                int id = Integer.parseInt(objects[0]);
                                String type = objects[1];
                                String location = objects[2];
                                double lat = Double.parseDouble(objects[3]);
                                double lon = Double.parseDouble(objects[4]);
                                LocalDateTime time = Util.stringToLocalDateTime(objects[5]);
                                sensor = new Sensor(id,type,lat,lon,location, metric);

                                // FIXME: weird thing happens on one input time
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
}