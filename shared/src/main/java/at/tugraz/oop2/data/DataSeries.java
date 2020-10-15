package at.tugraz.oop2.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.TreeSet;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public final class DataSeries extends TreeSet<DataPoint> {

    @NonNull
    private final Sensor sensor;
    @NonNull
    private Operation operation;
    private int interval; //interval in seconds

    public DataSeries(@NonNull Sensor sensor, int interval, Operation operation) {
        this.sensor = sensor;
        this.operation = operation == null ? Operation.NONE : operation;
        this.interval = interval;
    }

    public enum Operation {
        NONE,
        MIN,
        MAX,
        MEAN,
        MEDIAN
    }
}


