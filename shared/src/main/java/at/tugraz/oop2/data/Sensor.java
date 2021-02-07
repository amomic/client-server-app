package at.tugraz.oop2.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

/**
 * Holds the combination of a location and a metric of a sensor.
 * <p>
 * One "real" sensor measures many metrics (e.g. pressure, humidity, and temperature),
 * but this class only represents one combination of location and metric. A "real" sensor
 * that measures three metrics is represented with three instances of this class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Sensor implements Serializable {

    @JsonProperty("id")
    private int id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("location")
    private String location;

    @JsonProperty("metric")
    private String metric;


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
