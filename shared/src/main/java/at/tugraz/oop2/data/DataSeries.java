package at.tugraz.oop2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public final class DataSeries extends TreeSet<DataPoint> {

    @NonNull
    @JsonProperty("sensor")
    private final Sensor sensor;

    @NonNull
    @JsonProperty("operation")
    private Operation operation;

    @JsonProperty("interval")
    private int interval; //interval in seconds

    public DataSeries(@NonNull Sensor sensor, int interval, Operation operation) {
        this.sensor = sensor;
        this.operation = operation == null ? Operation.NONE : operation;
        this.interval = interval;
    }

    public double[] getValues() {
        List<Double> values = new ArrayList<>();
        for (DataPoint p : this) {
            values.add(p.getValue());
        }
        return values.stream()
                .mapToDouble(Number::doubleValue)
                .toArray();
    }

    public double[] getMinimumAndMaximumValue() {
        double minimum = Double.MAX_VALUE;
        double maximum = Double.MIN_VALUE;
        for (DataPoint p : this) {
            if (p.getValue() < minimum) {
                minimum = p.getValue();
            }
            if (p.getValue() > maximum) {
                maximum = p.getValue();
            }
        }
        return new double[]{minimum, maximum};
    }


    public LocalDateTime getFrom() {
        LocalDateTime from = LocalDateTime.MAX;
        for (DataPoint point : this) {
            if (point.getTime().isBefore(from)) {
                from = point.getTime();
            }
        }
        return from;
    }


    public LocalDateTime getTo() {
        LocalDateTime to = LocalDateTime.MIN;
        for (DataPoint point : this) {
            if (point.getTime().isAfter(to)) {
                to = point.getTime();
            }
        }
        return to;
    }

    public enum Operation {
        NONE, MIN,
        MAX, MEAN,
        MEDIAN
    }
}


