package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// TODO data command: interpolation + missing sequence + median

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
            } else if (msg instanceof DataQueryParameters) {
                DataQueryParameters parameters = (DataQueryParameters) msg;
                Logger.serverRequestData(parameters);
                DataSeries dataSeries = queryData(parameters);
                outputStream.writeObject(dataSeries);
                outputStream.reset();
                Logger.serverResponseData(parameters, dataSeries);
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
        List<DataPoint> result = new ArrayList<>();
        getData(file, parameters, sensor, dataPoints);

        // TODO: Operations calculations
        switch (parameters.getOperation()) {
            case NONE:
                result.addAll(dataPoints);
                break;
            case MIN:
                double min = dataPoints.stream()
                        .mapToDouble(DataPoint::getValue)
                        .min()
                        .getAsDouble();
                DataPoint minData = dataPoints.stream()
                        .filter(dataPoint -> dataPoint.getValue() == min)
                        .findFirst().orElse(new DataPoint(LocalDateTime.now(), min));
                result.add(minData);
                break;
            case MAX:
                double max = dataPoints.stream()
                        .mapToDouble(DataPoint::getValue)
                        .max()
                        .getAsDouble();
                DataPoint maxData = dataPoints.stream()
                        .filter(dataPoint -> dataPoint.getValue() == max)
                        .findFirst().orElse(new DataPoint(LocalDateTime.now(), max));
                result.add(maxData);
                break;
            case MEAN:
                double avg = dataPoints.stream()
                        .mapToDouble(DataPoint::getValue)
                        .average()
                        .getAsDouble();
                DataPoint avgData = dataPoints.stream()
                        .filter(dataPoint -> dataPoint.getValue() == avg)
                        .findFirst().orElse(new DataPoint(LocalDateTime.now(), avg));
                result.add(avgData);
                break;
            // TODO: sort the values and get the middle one, in case you got 2 values, get average of them
            case MEDIAN:
                break;
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