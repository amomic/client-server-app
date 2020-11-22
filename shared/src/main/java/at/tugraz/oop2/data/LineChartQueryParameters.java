package at.tugraz.oop2.data;

import java.time.LocalDateTime;

public class LineChartQueryParameters extends DataQueryParameters {
   private final String path;

    public LineChartQueryParameters(int sensorId, String metric, LocalDateTime from, LocalDateTime to,
                                    DataSeries.Operation operation, String path, long interval) {
        super(sensorId, metric, from, to, operation,interval);
        this.path = path;
    }
}
