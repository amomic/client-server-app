package at.tugraz.oop2.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SOMQueryParameters {
    private final List<Integer> sensorIds;
    private final String metric;
    private final LocalDateTime from;
    private final LocalDateTime to;
    private final DataSeries.Operation operation;
    private final long interval;
    private final int length;

    private final int gridHeight;
    private final int gridWidth;

    private final double updateRadius;
    private final double learningRate;
    private final long iterationsPerCurve;

    private final int resultId;
    private final int amountOfIntermediateResults;
}
