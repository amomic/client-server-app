package at.tugraz.oop2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * Holds the combination of a location and a metric of a sensor.
 * <p>
 * One "real" sensor measures many metrics (e.g. pressure, humidity, and temperature),
 * but this class only represents one combination of location and metric. A "real" sensor
 * that measures three metrics is represented with three instances of this class.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Sensor implements Serializable {

    @JsonProperty("id")
    private final int id;

    @JsonProperty("type")
    private final String type;

    @JsonProperty("latitude")
    private final double latitude;

    @JsonProperty("longitude")
    private final double longitude;

    @JsonProperty("location")
    private final String location;

    @JsonProperty("metric")
    private final String metric;


    public String prettyString() {
        return String.format("%s - %s", getLocation(), getMetric());
    }

    public int getId() {
        return id;
    }

    public String getMetric() {
        return metric;
    }


}
