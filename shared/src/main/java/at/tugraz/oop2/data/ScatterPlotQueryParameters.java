package at.tugraz.oop2.data;
import java.time.LocalDateTime;


public class ScatterPlotQueryParameters extends DataQueryParameters {

    public ScatterPlotQueryParameters(int sensorId, String metric, LocalDateTime from, LocalDateTime to,
                                      DataSeries.Operation operation, long interval) {
        super(sensorId, metric, from, to, operation, interval);
    }
}
