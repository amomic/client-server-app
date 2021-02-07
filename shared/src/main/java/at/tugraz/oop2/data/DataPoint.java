package at.tugraz.oop2.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents one data point in a data series. The meaning of the
 * field <b>value</b> is defined in the class <b>DataSeries</b>.
 * <p>
 * For example, the meaning (metric) of the field <b>value</b> can be <b>°C</b>
 * for temperature, <b>%</b> for relative humidity, and more.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public final class DataPoint implements Comparable<DataPoint>, Serializable {

    @JsonProperty("time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime time;

    @JsonProperty("value")
    private double value;

    @Override
    public int compareTo(DataPoint dataPoint) {
        int dateComparision = time.compareTo(dataPoint.getTime());
        return dateComparision == 0 ? Double.compare(this.getValue(), value) : dateComparision;
    }


    public DataPoint(DataPoint other)
    {
        this.time = other.getTime();
        this.value = other.getValue();
    }
}
