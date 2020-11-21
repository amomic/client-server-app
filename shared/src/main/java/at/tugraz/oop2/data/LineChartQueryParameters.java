package at.tugraz.oop2.data;

import java.time.LocalDateTime;

public class LineChartQueryParameters extends DataQueryParameters {
    public LineChartQueryParameters(int sensorId, String metric, LocalDateTime from, LocalDateTime to, DataSeries.Operation operation, long interval) {
        super(sensorId, metric, from, to, operation, interval);
    }
}
