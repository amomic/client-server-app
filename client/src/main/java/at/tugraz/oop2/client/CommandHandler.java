package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;

import java.awt.*;
import java.io.ObjectInputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

/**
 * Used for handling and parsing commands. There is little to no work to do for you here, since
 * the CLI is already implemented.
 */
public final class CommandHandler {
    private static final String MSG_HELP = "Type 'help' for a list of commands.";

    private final ClientConnection conn;
    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler(ClientConnection conn) {
        this.conn = conn;
        commands.put("help", this::displayHelp);
        commands.put("ls", this::listSensors);
        commands.put("data", this::queryData);
        commands.put("linechart", this::queryLineChart);
        commands.put("scatterplot", this::queryScatterplot);
    }

    private static void validateArgc(String[] args, int argc) throws CommandException {
        if (args.length != argc) {
            throw new CommandException("Invalid usage. " + MSG_HELP);
        }
    }

    private static void validateArgc(String[] args, int minArgc, int maxArgc) throws CommandException {
        if (args.length < minArgc || args.length > maxArgc) {
            throw new CommandException("Invalid usage. " + MSG_HELP);
        }
    }

    private static void printDataPoint(DataPoint point) {
        System.out.println("\t" + point.getTime() + "\t" + point.getValue());
    }

    public void handle(String... args) {
        final Command cmd = commands.get(args[0].toLowerCase());
        if (cmd == null) {
            System.out.println("Unknown command. " + MSG_HELP);
            return;
        }
        try {
            Logger.info("Client command entered!");
            cmd.handle(Arrays.copyOfRange(args, 1, args.length));
        } catch (final CommandException | NumberFormatException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openCLI() {
        System.out.println("Welcome to the command line interface. " + MSG_HELP);
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                final String line;
                System.out.print("> ");
                try {
                    line = scanner.nextLine().trim();
                } catch (final NoSuchElementException ex) { // EOF
                    break;
                }
                if (line.startsWith("#")) {
                    System.out.println(line);
                } else if (line.equalsIgnoreCase("exit")) {
                    break;
                } else if (!line.isEmpty()) {
                    handle(line.split("\\s+"));
                }
            }
        }
        System.out.println("Bye!");
    }

    private void listSensors(String... args) throws Exception {
        validateArgc(args, 0);
        Logger.clientRequestLS();
        System.out.println("Client request is sent!");
        //TODO print Sensors (not just use the Logger::clientResponseLS) -> DONE
        final List<Sensor> sensors = conn.querySensors().get();
        Logger.clientResponseLS(sensors);
        System.out.println("Client response:");
        System.out.println("| ----------------------------------------------------------------------------------------------------------------------------------------------|");
        System.out.println("|           Id          |          Type         |      Location        |         Lat            |          Lon       |         Metric           |");
        System.out.println("| ----------------------------------------------------------------------------------------------------------------------------------------------|");

        sensors.forEach(sensor -> {
            //sensor_id;sensor_type;location;lat;lon;timestamp;P1;durP1;ratioP1;P2;durP2;ratioP2
            String line = String.format("|  %20s |  %20s |  %20s |  %20s |  %20s |  %20s |", String.valueOf(sensor.getId()),
                    sensor.getType(), sensor.getLocation(), String.valueOf(sensor.getLatitude()),
                    String.valueOf(sensor.getLongitude()), sensor.getMetric());
            System.out.println(line);
        });
    }

    private static String getNewline() {
        return "\n         ";
    }

    private void queryData(String... args) throws Exception {
        validateArgc(args, 4, 6);
        final int sensorId = Integer.parseUnsignedInt(args[0]);
        final String type = args[1];
        final LocalDateTime from = Util.stringToLocalDateTime(args[2]);
        final LocalDateTime to = Util.stringToLocalDateTime(args[3]);
        final DataSeries.Operation operation = args.length < 5 ? DataSeries.Operation.NONE : DataSeries.Operation.valueOf(args[4].toUpperCase());
        final long interval = args.length < 6 ? from.until(to, ChronoUnit.SECONDS) : Util.stringToInterval(args[5]);

        final DataQueryParameters dataQueryParameters = new DataQueryParameters(sensorId, type, from, to, operation, interval);
        Logger.clientRequestData(dataQueryParameters);
        System.out.println("Client request is sent!");
        final DataSeries series = conn.queryData(dataQueryParameters).get();

        if(series.size() == 0) {
            Logger.err("No response from the server.");
        } else {
            Logger.clientResponseData(dataQueryParameters, series);
            System.out.println("Client response:");
            System.out.println("| ----------------------------------------------|");
            System.out.println("|      Timestamp        |         Value         |");
            System.out.println("| ----------------------------------------------|");

            series.forEach((DataPoint datapoint) -> {
                String line = String.format("|  %20s |  %20s | ", datapoint.getTime().toString(), String.valueOf(datapoint.getValue()));
                System.out.println(line);
            });
            System.out.println("| ----------------------------------------------|");
        }
        //TODO print Sensors (not just use the Logger::clientResponseData) -> DONE
    }

    private void queryLineChart(String... args) throws Exception {
        //TODO input parsing similar to above
        validateArgc(args, 4, 7);
        final int sensorId = Integer.parseUnsignedInt(args[0]);
        final String type = args[1];
        final LocalDateTime from = Util.stringToLocalDateTime(args[2]);
        final LocalDateTime to = Util.stringToLocalDateTime(args[3]);
        String path = args[4];
        final DataSeries.Operation operation = args.length < 6 ? DataSeries.Operation.NONE : DataSeries.Operation.valueOf(args[5].toUpperCase());
        final long interval = args.length < 7 ? from.until(to, ChronoUnit.SECONDS) : Util.stringToInterval(args[6]);


       // operation = DataSeries.Operation.MAX;
        final DataQueryParameters lineChartQueryParameters= new LineChartQueryParameters(sensorId, type, from, to, operation, path, interval);
        Logger.clientRequestData(lineChartQueryParameters);
        final DataSeries dataSeries = conn.queryLineChart(lineChartQueryParameters).get();

        if(dataSeries.size() == 0) {
            Logger.err("No response from the server.");
        } else {

            //TODO logger min, max where to get the values and mean??

            DataPoint min = dataSeries.first();
            DataPoint max = dataSeries.first();

            for (DataPoint d:dataSeries){
                if ( d.getValue() < min.getValue() )
                    min = d;
                if ( d.getValue() > max.getValue() )
                    max = d;

            }

            Picture lineChart = createLineChart(dataSeries, from, to, interval, min, max);
            lineChart.save(path);


            //TODO CALCULATE MEAN
            Logger.clientCreateLinechartImage(path, dataSeries, min, max, new DataPoint(null, 0) );
            System.out.println("| ----------------------------------------------|");
            System.out.println("| END LINECHART |");
            System.out.println("| ----------------------------------------------|");

        }
        //png can be created using the Picture class
    }

    private Picture createLineChart(DataSeries dataSeries, LocalDateTime from, LocalDateTime to, long interval, DataPoint min, DataPoint max) {
        int width = 1024;
        int height = 720;


        Picture lineChart = new Picture(width, height);
        Graphics2D graphics = lineChart.getGraphics2D();
        graphics.setBackground(Color.getColor("white"));

        // Y-Axis
        int x_offset = (int)(width*0.05);
        int y_offset = (int)(height*0.95);
        graphics.drawLine( x_offset ,(int)(height*0.05),x_offset, y_offset);
        // X-Axis
        graphics.drawLine( x_offset,y_offset,(int)(width*0.95),y_offset);

        double from_epoch = from.toEpochSecond(ZoneOffset.UTC);
        double to_epoch = to.toEpochSecond(ZoneOffset.UTC);


        //TODO CHECK timezone
        double normalized_val_tmp = normalize( dataSeries.first().getValue(), min.getValue(), max.getValue() );
        double normalized_time_tmp = normalize( dataSeries.first().getTime().toEpochSecond(ZoneOffset.UTC), from_epoch, to_epoch);
        for (DataPoint d: dataSeries){
            double normalized_val = normalize( d.getValue(), min.getValue(), max.getValue() );
            double normalized_time = normalize( d.getTime().toEpochSecond(ZoneOffset.UTC), from_epoch, to_epoch);

            graphics.drawLine( (int)(width*normalized_time),(int)(height*normalized_val),(int)(width*normalized_time_tmp),
                    (int)(height*normalized_val_tmp));

            normalized_val_tmp = normalized_val;
            normalized_time_tmp = normalized_time;
        }

        return lineChart;
    }

    double normalize(double value, double min, double max) {
        double first_normal = 1 - ((value - min) / (max - min));
        return first_normal * 0.90;
    }


    private void queryScatterplot(String... args) throws Exception {
        //TODO input parsing similar to above
        validateArgc(args, 6, 8);
        final int sensorId1 = Integer.parseUnsignedInt(args[0]);
        final int sensorId2 = Integer.parseUnsignedInt(args[2]);
        final String type1 = args[1];
        final String type2 = args[3];
        final LocalDateTime from = Util.stringToLocalDateTime(args[4]);
        final LocalDateTime to = Util.stringToLocalDateTime(args[5]);
        final DataSeries.Operation operation = args.length < 8 ? DataSeries.Operation.NONE : DataSeries.Operation.valueOf(args[6].toUpperCase());
        final long interval = args.length < 8 ? from.until(to, ChronoUnit.SECONDS) : Util.stringToInterval(args[7]);

        final ScatterPlotQueryParameters scatterPlotQueryParameters = new ScatterPlotQueryParameters(sensorId1,
                type1, from, to, operation, interval, sensorId2, type2);

        Logger.clientRequestData(scatterPlotQueryParameters);
        System.out.println("Client request is sent!");

        /*final Picture picture = conn.queryScatterPlot().get();

        if(picture == null) {
            Logger.err("No response from the server.");
        } else {
            //Logger.clientCreateLinechartImage("ime.png", series);
            picture.save("scatterplot.png");
            System.out.println("| ----------------------------------------------|");
            System.out.println("| END SCATTERPLOT |");
            System.out.println("| ----------------------------------------------|");

        }
        //png can be created using the Picture class*/
    }

    private void displayHelp(String... args) {
        System.out.println("Usage:");
        System.out.println("  ls\t- Lists all sensors and metrics.");
        System.out.println("  data <sensorId> <metric> <from-time> <to-time> [operation [interval<s|m|h|d>]]\t- Displays historic values measured by a sensor.");
        System.out.println("  linechart <sensorId> <metric> <from-time> <to-time> [operation [interval<s|m|h|d>]]\t- Creates a Linechart png with values measured by a sensor.");
        System.out.println("  scatterplot <sensorId1> <metric1> <sensorId2> <metric2> <from-time> <to-time> [operation [interval<s|m|h|d>]]\t- Creates a Scatterplot png with values measured by two sensors.");
        System.out.println("  exit\t- Terminate the CLI.");
        System.out.println("More information is contained in the assignment description and in the folder queries/.");
        System.out.println();
    }

    @FunctionalInterface
    private interface Command {
        void handle(String... args) throws Exception;
    }

    private static final class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }
}
