package at.tugraz.oop2;

import at.tugraz.oop2.data.DataPoint;
import at.tugraz.oop2.data.DataQueryParameters;
import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.Sensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * You may use Logger.info() Logger.warn() Logger.err() wherever you want in
 * your code. We will not check the console output produced by these methods.
 * However, make sure to call the other methods as described below.
 * <p>
 * We will replace this file for testing purposes. You may alter this class, but
 * make sure to be input - output conform.
 */

public class Logger {


    private static final boolean OUTPUTHIDDEN = true;

    // https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    // In case you encounter problems with printing color to console and you want to remove it:
    // Just set all the ANSI_* constants below to an empty String.
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static String now() {
        return LocalDateTime.now().toString() + " ";
    }

    // public logging methods to be used anywhere

    public static void info(String msg) {
        if (OUTPUTHIDDEN) return;
        println(ANSI_GREEN + "[ INFO    ] " + ANSI_RESET + msg);
    }

    public static void warn(String msg) {
        if (OUTPUTHIDDEN) return;
        println(ANSI_YELLOW + "[ WARNING ] " + ANSI_RESET + msg);
    }

    public static void err(String msg) {
        if (OUTPUTHIDDEN) return;
        println(ANSI_RED + "[ ERROR   ] " + ANSI_RESET + msg);
    }

    // public logging methods that need to be used as described in the assignment specification

    public static void clientRequestLS() {
        //TODO should be executed on the clientside when a LS request is made
        if (OUTPUTHIDDEN) return;
        println(getClientRequest() + "Sensors.");
    }

    public static void serverRequestLS() {
        //TODO should be executed on the serverside when a LS request is made
        if (OUTPUTHIDDEN) return;
        println(getServerRequest() + "Sensors.");
    }

    public static void serverResponseLS(List<Sensor> data) {
        //TODO should be executed on the serverside when a LS response is sent
        if (OUTPUTHIDDEN) return;
        println(getServerResponse() + "Sensors:" + getFormattedSensorList(data));
    }

    public static void clientResponseLS(List<Sensor> data) {
        //TODO should be executed on the clientside when a LS response is received
        if (OUTPUTHIDDEN) return;
        println(getClientResponse() + "Sensors:" + getFormattedSensorList(data));
    }

    public static void clientRequestData(DataQueryParameters params) {
        //TODO should be executed on the clientside when a Data request is made
        if (OUTPUTHIDDEN) return;
        println(getClientRequest() + "Data:" + getNewline() + getFormattedParameters(params));
    }

    public static void serverRequestData(DataQueryParameters params) {
        //TODO should be executed on the serverside when a Data request is made
        if (OUTPUTHIDDEN) return;
        println(getServerRequest() + "Data:" + getNewline() + getFormattedParameters(params));
    }

    public static void serverResponseData(DataQueryParameters params, DataSeries data) {
        //TODO should be executed on the serverside when a Data response is sent
        if (OUTPUTHIDDEN) return;
        println(getServerResponse() + "Data:" + getNewline() + getFormattedParameters(params) + getNewline() + getFormattedDataSeries(data));
    }

    public static void clientResponseData(DataQueryParameters params, DataSeries data) {
        //TODO should be executed on the clientside when a Data response is received
        if (OUTPUTHIDDEN) return;
        println(getClientResponse() + "Data:" + getNewline() + getFormattedParameters(params) + getNewline() + getFormattedDataSeries(data));
    }


    public static void serverCacheHit(Sensor sensor, LocalDateTime from, LocalDateTime to) {
        //TODO should be executed on the serverside when the requested Data is available inside the cache
        if (OUTPUTHIDDEN) return;
        println(getServerAction() + "CacheHit:" + getNewline() + getFormattedCacheDetail(sensor, from, to));
    }

    public static void serverCacheMiss(Sensor sensor, LocalDateTime from, LocalDateTime to) {
        //TODO should be executed on the serverside when the requested Data is not available inside the cache
        if (OUTPUTHIDDEN) return;
        println(getServerAction() + "CacheMiss:" + getNewline() + getFormattedCacheDetail(sensor, from, to));
    }

    public static void serverDiskRequest(String path) {
        //TODO should be executed on the serverside when a csv file is loaded from disk
        if (OUTPUTHIDDEN) return;
        println(getServerAction() + "DiskRequest:" + getNewline() + path);
    }

    public static void clientCreateLinechartImage(String path, DataSeries data, DataPoint min, DataPoint max,
                                                  DataPoint mean) {
        //TODO should be executed on the clientside when a Linechart image is created
        if (OUTPUTHIDDEN) return;
        println(getClientAction() + "Linechart:" + getNewline() + "min: \"" + min.getValue() + "\"" + getNewline() +
                "mean: \"" + mean.getValue() + "\"" + getNewline() + "max: \"" + max.getValue() + "\"" + getNewline() +
                "Image located at: \"" + path + "\"" + getNewline() + getFormattedDataSeries(data));
    }

    public static void clientCreateScatterplotImage(String path, DataSeries dataXaxis, DataSeries dataYaxis) {
        //TODO should be executed on the clientside when a Scatterplot image is created
        if (OUTPUTHIDDEN) return;
        println(getClientAction() + "Scatterplot:" + getNewline() + "Image located at: \"" + path + "\"" + getNewline() + "x Data: " + getNewline() + getFormattedDataSeries(dataXaxis) + getNewline() + "y Data: " + getNewline() + getFormattedDataSeries(dataYaxis));
    }

    private static String getClientRequest() {
        return ANSI_BLUE + "CLIENT - REQUEST:" + ANSI_RESET + "  ";
    }

    private static String getClientResponse() {
        return ANSI_PURPLE + "CLIENT - RESPONSE:" + ANSI_RESET + " ";
    }

    private static String getClientAction() {
        return ANSI_PURPLE + "CLIENT - ACTION:" + ANSI_RESET + " ";
    }

    private static String getServerAction() {
        return ANSI_PURPLE + "SERVER - ACTION:" + ANSI_RESET + " ";
    }

    private static String getServerRequest() {
        return ANSI_BLUE + "SERVER - REQUEST:" + ANSI_RESET + "  ";
    }

    private static String getServerResponse() {
        return ANSI_PURPLE + "SERVER - RESPONSE:" + ANSI_RESET + " ";
    }

    private static String getFormattedSensorList(List<Sensor> data) {
        List<Sensor> sorted_data = new ArrayList<Sensor>(data);
        sorted_data.sort((Sensor a, Sensor b) -> {
            if (a.getLocation().compareTo(b.getLocation()) == 0) {
                return a.getMetric().compareTo(b.getMetric());
            } else {
                return a.getLocation().compareTo(b.getLocation());
            }
        });
        StringBuilder content = new StringBuilder();
        sorted_data.forEach((Sensor sensor) -> {
            content.append(getNewline());
            content.append("id: \"");
            content.append(sensor.getId());
            content.append("\" type: \"");
            content.append(sensor.getType());
            content.append("\" latitude: \"");
            content.append(sensor.getLatitude());
            content.append("\" longitude: \"");
            content.append(sensor.getLongitude());
            content.append("\" location: \"");
            content.append(sensor.getLocation());
            content.append("\" metric: \"");
            content.append(sensor.getMetric());
            content.append("\"");
        });
        return content.toString();
    }

    private static String getFormattedCacheDetail(Sensor sensor, LocalDateTime from, LocalDateTime to) {
        return "sensorId:   \"" + sensor.getId() + "\"" + getNewline() +
                "metric:      \"" + sensor.getMetric() + "\"" + getNewline() +
                "from:      \"" + from + "\"" + getNewline() +
                "to:        \"" + to + "\"";
    }

    private static String getFormattedParameters(DataQueryParameters params) {
        return "sensorId:   \"" + params.getSensorId() + "\"" + getNewline() +
                "type:      \"" + params.getMetric() + "\"" + getNewline() +
                "from:      \"" + params.getFrom() + "\"" + getNewline() +
                "to:        \"" + params.getTo() + "\"" + getNewline() +
                "operation: \"" + params.getOperation() + "\"" + getNewline() +
                "interval:  \"" + params.getInterval() + "\"";
    }


    private static String getFormattedDataSeries(DataSeries data) {

        if (data == null) {
            throw new UnsupportedOperationException("This should not happen! If there is no Data available you should return a empty DataSeries.");
        }

        StringBuilder content = new StringBuilder();
        content.append("| -------------------------------------- |");
        content.append(getNewline());
        content.append("|          Time          |     value     |");
        content.append(getNewline());
        content.append("| -------------------------------------- |");
        data.forEach((DataPoint x) -> {
            content.append(getNewline());
            content.append(String.format("|  %20s ", x.getTime().toString()));
            content.append(" | ");
            content.append(String.format("%13.3f", x.getValue()));
            content.append(" |");
        });
        content.append(getNewline());
        content.append("| -------------------------------------- |");
        return content.toString();
    }


    private static void println(String msg) {
        System.out.println(now() + msg);
    }

    private static String getNewline() {
        return "\n         ";
    }


}


