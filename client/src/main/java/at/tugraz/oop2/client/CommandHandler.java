package at.tugraz.oop2.client;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.DataPoint;
import at.tugraz.oop2.data.DataQueryParameters;
import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.Sensor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        //TODO print Sensors (not just use the Logger::clientResponseLS) -> DONE
        final List<Sensor> sensors = conn.querySensors().get();
        Logger.clientResponseLS(sensors);

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
        final DataSeries series = conn.queryData(dataQueryParameters).get();

        if(series.size() == 0) {
            Logger.err("No response from the server.");
        } else {
            Logger.clientResponseData(dataQueryParameters, series);
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
        //png can be created using the Picture class
    }

    private void queryScatterplot(String... args) throws Exception {
        //TODO input parsing similar to above
        //png can be created using the Picture class
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
