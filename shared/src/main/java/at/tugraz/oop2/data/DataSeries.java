package at.tugraz.oop2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

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

    public enum Operation {
        NONE, MIN,
        MAX, MEAN,
        MEDIAN
    }
}


