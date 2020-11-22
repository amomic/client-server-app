package at.tugraz.oop2.data;
import java.time.LocalDateTime;


public class ScatterPlotQueryParameters extends DataQueryParameters {
    private final int sensorId2;
    private final String metric2;

    public ScatterPlotQueryParameters(int sensorId, String metric, LocalDateTime from, LocalDateTime to,
                                      DataSeries.Operation operation, long interval, int sensorId2, String metric2) {
        super(sensorId, metric, from, to, operation, interval);
        this.sensorId2 = sensorId2;
        this.metric2 = metric2;
    }
}
