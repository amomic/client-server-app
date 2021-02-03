package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.*;
import lombok.SneakyThrows;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

/**
 * Used for handling and parsing commands. There is little to no work to do for you here, since
 * the CLI is already implemented.
 */
public final class CommandHandler {
    private static final String MSG_HELP = "Type 'help' for a list of commands.";

    private final ClientConnection conn;
    private final Map<String, Command> commands = new HashMap<>();
    private DataSeries dataSeries1;


    public CommandHandler(ClientConnection conn) {
        this.conn = conn;
        commands.put("help", this::displayHelp);
        commands.put("ls", this::listSensors);
        commands.put("data", this::queryData);
        commands.put("linechart", this::queryLineChart);
        commands.put("scatterplot", this::queryScatterplot);
        // added commands
        commands.put("cluster", this::queryCluster);
        commands.put("plotcluster", this::queryPlotCluster);
        commands.put("listresults", this::listResults);
        commands.put("rm", this::removeResults);
        commands.put("inspectcluster", this::inspectCluster);

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

        final List<Sensor> sensors = conn.querySensors().get();

        Logger.clientResponseLS(sensors);
        System.out.println("Client response:");
        System.out.println("| ----------------------------------------------------------------------------------------------------------------------------------------------|");
        System.out.println("|           Id          |          Type         |      Location        |         Lat            |          Lon       |         Metric           |");
        System.out.println("| ----------------------------------------------------------------------------------------------------------------------------------------------|");

        sensors.forEach(sensor -> {
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
            Logger.err("Two or more missing data points. No response from the server.");
            System.out.println("Two or more missing data points. No response from the server.");
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
    }

    private void queryLineChart(String... args) throws Exception {
        validateArgc(args, 4, 7);
        final int sensorId = Integer.parseUnsignedInt(args[0]);
        final String type = args[1];
        final LocalDateTime from = Util.stringToLocalDateTime(args[2]);
        final LocalDateTime to = Util.stringToLocalDateTime(args[3]);
        String path = args[4];
        final DataSeries.Operation operation = args.length < 6 ? DataSeries.Operation.NONE : DataSeries.Operation.valueOf(args[5].toUpperCase());
        final long interval = args.length < 7 ? from.until(to, ChronoUnit.SECONDS) : Util.stringToInterval(args[6]);

        final DataQueryParameters lineChartQueryParameters= new LineChartQueryParameters(sensorId, type, from, to, path, operation, interval);

        Logger.clientRequestData(lineChartQueryParameters);
        System.out.println("Client request is sent!");

        final DataSeries dataSeries = conn.queryLineChart(lineChartQueryParameters).get();

        if(dataSeries.size() == 0) {
            Logger.err("Two or more missing data points. No response from the server.");
            System.out.println("Two or more missing data points. No response from the server.");
        } else {
            DataPoint min = dataSeries.first();
            DataPoint max = dataSeries.first();

            for (DataPoint d:dataSeries) {
                if ( d.getValue() < min.getValue() )
                    min = d;
                if ( d.getValue() > max.getValue() )
                    max = d;

            }

            double time_val = 0.00;
            float time_val_end = 0;

            for(DataPoint d : dataSeries)
            {

                time_val += (double) d.getTime().toEpochSecond(ZoneOffset.UTC);

            }
            time_val_end =(float)time_val / dataSeries.size();

            double mean_val = dataSeries.stream().mapToDouble(DataPoint::getValue).average().orElse(0);
            LocalDateTime mean_time = LocalDateTime.ofEpochSecond((long)time_val_end,0,ZoneOffset.UTC);

            DataPoint mean = new DataPoint(mean_time,mean_val);

            Picture lineChart = createLineChart(dataSeries, from, to, interval, min, max);
            lineChart.save(path);

            Logger.clientCreateLinechartImage(path, dataSeries, min, max, mean);

            System.out.println("Client action: linechart. Data points displayed:");
            System.out.println("| -------------------------------------------------------|");
            System.out.println("|           Time             |               Value       |");
            System.out.print("|");
            printDataPoint(min);
            System.out.print("|");
            printDataPoint(max);
            System.out.print("|");
            printDataPoint(mean);
            System.out.println("| -------------------------------------------------------|");

        }
    }

    private Picture createLineChart(DataSeries dataSeries, LocalDateTime from, LocalDateTime to, long interval, DataPoint min, DataPoint max) {
        int width = 1100;
        int height = 900;


        Picture lineChart = new Picture(width, height);
        Graphics2D graphics = lineChart.getGraphics2D();

        // Y-Axis
        int x_offset = (int)(width*0.05);
        int y_offset = (int)(height*0.95);
        graphics.drawLine(x_offset ,(int)(height*0.05),x_offset, y_offset);
        // X-Axis
        graphics.drawLine(x_offset,y_offset,(int)(width*0.95),y_offset);

        double from_epoch = from.toEpochSecond(ZoneOffset.UTC);
        double to_epoch = to.toEpochSecond(ZoneOffset.UTC);

        //time
        graphics.drawString(String.valueOf(from),(int)(width*0.06),(int)(height*0.97));
        graphics.drawString(String.valueOf(to),(int)(width*0.85),(int)(height*0.97));
        //value
        graphics.drawString((String.valueOf(min.getValue())),(int)(width*0.01),(int)(height*0.94));
        graphics.drawString((String.valueOf(max.getValue())),(int)(width*0.02),(int)(height*0.04));


        double normalized_val_tmp = normalize(dataSeries.first().getValue(), min.getValue(), max.getValue() );
        double normalized_time_tmp = normalize(dataSeries.first().getTime().toEpochSecond(ZoneOffset.UTC), from_epoch, to_epoch);
        for (DataPoint d: dataSeries) {
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
        return first_normal*0.90;
    }

    private void queryScatterplot(String... args) throws Exception {
        validateArgc(args, 7, 9);
        final int sensorId1 = Integer.parseUnsignedInt(args[0]);
        final int sensorId2 = Integer.parseUnsignedInt(args[2]);
        final String type1 = args[1];
        final String type2 = args[3];
        final LocalDateTime from = Util.stringToLocalDateTime(args[4]);
        final LocalDateTime to = Util.stringToLocalDateTime(args[5]);
        String path = args[6];
        final DataSeries.Operation operation = args.length < 8 ? DataSeries.Operation.NONE : DataSeries.Operation.valueOf(args[7].toUpperCase());
        final long interval = args.length < 9 ? from.until(to, ChronoUnit.SECONDS) : Util.stringToInterval(args[8]);

        final ScatterPlotQueryParameters scatterPlotQueryParameters1 = new ScatterPlotQueryParameters(sensorId1, type1,from, to, operation, interval);

        final ScatterPlotQueryParameters scatterPlotQueryParameters2 = new ScatterPlotQueryParameters(sensorId2, type2,from, to, operation, interval);

        Logger.clientRequestData(scatterPlotQueryParameters1);
        Logger.clientRequestData(scatterPlotQueryParameters2);
        System.out.println("Client request is sent!");

        final DataSeries dataSeries1 = conn.queryLineChart(scatterPlotQueryParameters1).get();
        final DataSeries dataSeries2 = conn.queryLineChart(scatterPlotQueryParameters2).get();

        if(dataSeries1.size() == 0 || dataSeries2.size()==0) {
            Logger.err("Two or more missing data points. No response from the server.");
            System.out.println("Two or more missing data points. No response from the server.");
        } else {
            DataPoint min1 = dataSeries1.first();
            DataPoint max1 = dataSeries1.first();

            DataPoint min2 = dataSeries2.first();
            DataPoint max2 = dataSeries2.first();

            for (DataPoint d:dataSeries1) {
                if ( d.getValue() < min1.getValue() )
                    min1 = d;
                if ( d.getValue() > max1.getValue() )
                    max1 = d;

            }
            for (DataPoint d:dataSeries2) {
                if ( d.getValue() < min2.getValue() )
                    min2 = d;
                if ( d.getValue() > max2.getValue() )
                    max2 = d;

            }

            Picture scatterPlot = createScatterPlot(dataSeries1, dataSeries2, min1, max1, min2, max2);
            scatterPlot.save(path);

            Logger.clientCreateScatterplotImage(path, dataSeries1, dataSeries2);

            System.out.println("Client action: scatterplot. Data points displayed:");
            System.out.println("| -------------------------------------------------------|");
            System.out.println("|           Time             |               Value       |");
            System.out.print("|");
            printDataPoint(min1);
            System.out.print("|");
            printDataPoint(max1);
            System.out.print("|");
            printDataPoint(min2);
            System.out.print("|");
            printDataPoint(max2);
            System.out.println("| -------------------------------------------------------|");

        }
    }

    private Picture createScatterPlot(DataSeries dataSeries1,DataSeries dataSeries2, DataPoint min1, DataPoint max1,DataPoint min2, DataPoint max2) {
        int width = 1100;
        int height = 900;


        Picture scatterPlot = new Picture(width, height);
        Graphics2D graphics = scatterPlot.getGraphics2D();

        // Y-Axis
        int x_offset = (int)(width*0.05);
        int y_offset = (int)(height*0.95);
        graphics.drawLine(x_offset ,(int)(height*0.05),x_offset, y_offset);
        // X-Axis
        graphics.drawLine(x_offset,y_offset,(int)(width*0.95),y_offset);


        //sensor 1 min max
        graphics.drawString(String.valueOf(min1.getValue()),(int)(width*0.01),(int)(height*0.94));
        graphics.drawString(String.valueOf(max1.getValue()),(int)(width*0.01),(int)(height*0.03));
        //sensor 2 min max
        graphics.drawString(String.valueOf(min2.getValue()),(int)(width*0.06),(int)(height*0.97));
        graphics.drawString(String.valueOf(max2.getValue()),(int)(width*0.90),(int)(height*0.97));


        DataPoint [] ds1 = dataSeries1.toArray(DataPoint[]::new);
        DataPoint [] ds2 = dataSeries2.toArray(DataPoint[]::new);

        for (int x=0; x<ds1.length; x++ ) {
            double val1 = normalize(ds1[x].getValue(),min1.getValue(), max1.getValue());
            double val2 = normalize(ds2[x].getValue(), min2.getValue(), max2.getValue());

            graphics.drawRect((int)(width*val1), (int)(height*val2), 10,10);
        }

        return scatterPlot;
    }

    // TODO: 2nd assignment implementation

    private void queryCluster(String... args) throws Exception {
        validateArgc(args, 14, 15);
        final String sensorIds = args[0];
        List<Integer> intList = new ArrayList<Integer>();
        if(sensorIds.equals("all"))
            intList.add(0);
        else{
            String sensorIdsSplit[] = sensorIds.split(",");
            for(String s : sensorIdsSplit) intList.add(Integer.valueOf(s));
        }

        final String type = args[1];
        final LocalDateTime from = Util.stringToLocalDateTime(args[2]);
        final LocalDateTime to = Util.stringToLocalDateTime(args[3]);
        final long interval = args.length < 5 ? from.until(to, ChronoUnit.SECONDS) : Util.stringToInterval(args[4]);
        final DataSeries.Operation operation =  args.length < 6 ? DataSeries.Operation.NONE : DataSeries.Operation.valueOf(args[5].toUpperCase());
        final int length =  Integer.parseUnsignedInt(args[6]);
        final int grid_length =  Integer.parseUnsignedInt(args[7]);
        final int grid_width =  Integer.parseUnsignedInt(args[8]);
        final double radius =  Double.parseDouble(args[9]);
        final double rate =  Double.parseDouble(args[10]);
        final int iterations =  Integer.parseUnsignedInt(args[11]);
        final int resultId = Integer.decode(args[12]);
        final int inter_results =  Integer.parseUnsignedInt(args[13]);
        List<ClusterDescriptor> series = null;

        final SOMQueryParameters somQueryParameters = new SOMQueryParameters(intList, type, from, to, operation, interval, length,grid_length,grid_width,radius,rate,
                iterations, resultId,inter_results);
        Logger.clientRequestCluster(somQueryParameters);

        long diff = Duration.between(somQueryParameters.getFrom(), somQueryParameters.getTo()).toSeconds();
        long numberOfIntervals = diff / somQueryParameters.getInterval();
        if (numberOfIntervals % somQueryParameters.getLength() != 0) {
            System.out.println("Exception will be thrown! Length value is not valid!");
            throw new Exception("Cannot divide into arrays of the same length");
        }
        try{
            series = conn.queryCluster(somQueryParameters).get();
        } catch(Exception e)
        {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        if(numberOfIntervals % somQueryParameters.getLength() == 0)
        {
            System.out.println("Client request is sent!");
            System.out.println("Client response:");
            System.out.println("          sensors:       " + sensorIds);
            System.out.println("          type:          " + type);
            System.out.println("          from:          " + from + " until " + to);

            System.out.println("          packing:       " + length + " DataPoints using " + operation + " with a sampling size of " + interval + " seconds");
            System.out.println("          SOM-Params;    " + "(" + grid_length + ", " + grid_width + ")" + " - Grid with a learning rate of " + rate + " and an initial update Radius of " + radius+ " times the diameter of the grid for " + iterations + " iterations per curve.");
            System.out.println("          ResultID:      " + resultId + " will contain " + inter_results + " intermediate results.");

            System.out.println("Check results in json file!");

        }
    }

    @SneakyThrows
    private void listResults(String... args) throws CommandException {
        validateArgc(args, 0, 1);
        String[] listResultIDs;
        System.out.println("Client action: Listing result IDs: ");
        File currentDir = new File("clusteringResults/"); // current directory

        listResultIDs = currentDir.list();

        for (String resultID : listResultIDs) {
            System.out.println(resultID);
        }

        Logger.clientListResults();
    }

    public static void getFiles(File dir) throws CommandException, IOException {
        File[] files = dir.listFiles();
        String inhalt = null;
        BufferedReader in = null;
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFiles(file);
                } else {
                    System.out.println(" file:" + file.getCanonicalPath());
                    in = new BufferedReader(new FileReader(file));
                    while ((inhalt = in.readLine()) != null) {
                        System.out.println(inhalt);
                    }
                }
            }
        } catch (CommandException | IOException e) {
            e.printStackTrace();
        }
    }

    private void removeResults(String... args) throws CommandException {
        validateArgc(args, 1);

        String id = args[0];
        int resultID = 0;
        if(id.contains("x")){
            resultID = Integer.decode(id);
        }
        else
            resultID = Integer.parseInt(id);
        System.out.println("Client request is sent!");
        String path = "clusteringResults/" + resultID;
        File delete_file = new File(path);
        File[] allContents = delete_file.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                file.delete();
            }
        }
        delete_file.delete();
        Logger.clientRemoveResult(resultID);
    }

    // TODO: plotcluster command
    private List<Integer> queryPlotCluster(String... args) throws Exception{
        final int resultId = Integer.parseUnsignedInt(args[0]);
        final int clusterPlotHeight = Integer.parseInt(args[1]);
        final int clusterPlotWidth = Integer.parseInt(args[2]);
        final boolean boolPlotClusterMembers = Boolean.parseBoolean(args[3]);
        final String heatMapOperation = args[4].toUpperCase();
        final boolean boolPlotAllFrames = Boolean.parseBoolean(args[5]);
        if (clusterPlotHeight <= 0 || clusterPlotWidth <= 0)
            Logger.err("Width and Height have to be positive.");
        System.out.println("Width and Height have to be positive.");
        final String cluster = Integer.toString(resultId);
        System.out.println("Client request is sent!");
        Logger.clientPlotCluster(resultId, boolPlotClusterMembers, heatMapOperation, boolPlotAllFrames);
        if (cluster == String.valueOf(0)) {
            Logger.err("Two or more missing cluster points. No response from the server.");
            System.out.println("Two or more missing cluster points. No response from the server.");
        }
        final List<Sensor> results = conn.queryResults().get();
        getFiles((File) results);
        final List<Integer> files = Collections.synchronizedList(new ArrayList<>());
        try {

            results.forEach((final Sensor p) -> {
                if (!boolPlotAllFrames )
                    System.out.println("Warning: Latest intermediate result found is not the finished result.");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.format("Wrote %d files", files.size()));
        if (!boolPlotAllFrames)
            return files;
        Optional<String> frames = files.parallelStream().sorted()
                .map((final Integer p) -> String.format("file 'iter_%d_clusterplot.json'", p))
                .reduce((final String prev, final String next) -> String.format("%s%n%s", prev, next));
        return files;
    }

    // TODO: inspectCluster command
    private void inspectCluster(String... args) throws CommandException {
        validateArgc(args, 3, 4);
        //final int resultID = Integer.parseUnsignedInt(args[0]);
        final int heightIndex = Integer.parseUnsignedInt(args[1]);
        final int widthIndex = Integer.parseUnsignedInt(args[2]);

        String id1 = args[0];
        int resultID1 = 0;
        if(id1.contains("x")){
            resultID1 = Integer.decode(id1);
        }
        else
            resultID1 = Integer.parseInt(id1);

        final boolean boolVerbose = args.length >= 4 && Boolean.parseBoolean(args[3]); // default false
        try {
            File cluster = new File("clusteringResults/");
            final File[] files = cluster.listFiles();
            if (files == null)
                throw new CommandException("Not a directory");

            final String line1 = "\t+------------------------------------------------+";
            final String line2= "\t+------------------+-----------------------------+";
            final String separeting3 = "\t+------------+------------+----------------------+";
            final String ln1 = "\t| %-46s |";
            final String l2f = "\t| %-16s | %27.5f |";
            final String ln3 = "\t| %-10s | %-10s | %20s |";


            System.out.println(line1);
            System.out.println(
                    String.format(ln1, String.format("Node (%d, %d) from %x", heightIndex, widthIndex, resultID1)));
            System.out.println(line2);
            System.out.println(String.format(l2f, String.format("#Members"), 0.0));
            System.out.println(line2);
            System.out.println(String.format(l2f, String.format("#Error"), -1.0));
            System.out.println(line2);
            System.out.println(String.format(l2f, String.format("#Entropy"), -1.0));
            System.out.println(line2);

            if (boolVerbose == true)
            {
                System.out.println(String.format(ln1, String.format("List of all members:")));
                System.out.println(separeting3);
                System.out.println(String.format(ln3, String.format("from"), String.format("to"), String.format("sensor")));
            }

        } catch (final NullPointerException ex) {
            Logger.warn(String.format("Exception inspecting: %s", ex.getMessage()));
            throw new CommandException("Could not  found inspect cluster");
        }
    }

    private void displayHelp(String... args) {
        System.out.println("Usage:");
        System.out.println("  ls\t- Lists all sensors and metrics.");
        System.out.println("  data <sensorId> <metric> <from-time> <to-time> [operation [interval<s|m|h|d>]]\t- Displays historic values measured by a sensor.");
        System.out.println("  linechart <sensorId> <metric> <from-time> <to-time> [operation [interval<s|m|h|d>]]\t- Creates a Linechart png with values measured by a sensor.");
        System.out.println("  scatterplot <sensorId1> <metric1> <sensorId2> <metric2> <from-time> <to-time> [operation [interval<s|m|h|d>]]\t- Creates a Scatterplot png with values measured by two sensors.");
        // added help command description
        System.out.println(
                "  cluster (all | <id>[,<id>]+) <metric> <from> <to> <interval> <operation> <length> <gridHeight> <gridWidth> <updateRadius> <learningRate> <iterationPerCurve> <resultID> <amountOfIntermediateResults>");
        System.out.println("              - SOM clustering operation on the server.");
        System.out.println("  listresults - List available intermediate results.");
        System.out.println("  rm          - Delete intermediate result.");
        System.out.println("  inspectcluster <resultID> <heightIndex> <widthIndex> <boolVerbose>");
        System.out.println("              - Show information about a cluster in intermediate result.");
        System.out.println(
                "  plotcluster <resultID> <clusterPlotHeight> <clusterPlotWidth> <boolPlotClusterMember> <heatMapOperation> <boolPlotAllFrames>");
        System.out.println("              - Plot cluster from intermediate result.");
        System.out.println("  exit        - Terminate the CLI.");
        System.out.println("More information is contained in the assignment description and in the folder queries/.");
        System.out.println();
    }


    @FunctionalInterface
    private interface Command {
        void handle(String... args) throws Exception;
    }

    static final class CommandException extends Exception {
        public CommandException(String message) {
            super(message);
        }
    }
}
