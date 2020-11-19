package at.tugraz.oop2.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DataQueryParameters implements Serializable {
    private final int sensorId;
    private final String metric;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final DataSeries.Operation operation;
    private final long interval;
}
