package at.tugraz.oop2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Holds some constants and useful methods.
 */
public final class Util {


    private Util() {

    }

    public static LocalDateTime stringToLocalDateTime(String datetime) {
        try {
            return LocalDate.parse(datetime, DateTimeFormatter.ISO_DATE).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(datetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
        }
        throw new IllegalArgumentException("Invalid date format " + datetime);
    }


    public static int stringToInterval(String interval) {
        if (interval.length() < 2) {
            throw new IllegalArgumentException("Invalid interval format " + interval);
        }
        try {
            int intervalPart = Integer.parseUnsignedInt(interval.substring(0, interval.length() - 1));
            char multiplicatorPart = interval.charAt(interval.length() - 1);
            int multiplicator = 1;

            switch (multiplicatorPart) {
                case 's':
                    multiplicator = 1;
                    break;
                case 'm':
                    multiplicator = 60;
                    break;
                case 'h':
                    multiplicator = 60 * 60;
                    break;
                case 'd':
                    multiplicator = 60 * 60 * 24;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid interval format " + interval);
            }

            return intervalPart * multiplicator;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid interval format " + interval);
        }

    }


    public static <T> List<List<T>> partitionList(List<T> objs, final int N) {
        return new ArrayList<>(IntStream.range(0, objs.size()).boxed().collect(
                Collectors.groupingBy(e -> e / N, Collectors.mapping(e -> objs.get(e), Collectors.toList())
                )).values());
    }


    public static double[] subtractVectors(double[] vectorA, double[] vectorB) {
        double[] result = new double[vectorA.length];
        for (int i = 0; i < vectorA.length; i++) {
            result[i] = vectorA[i] - vectorB[i];
        }
        return result;
    }


}
