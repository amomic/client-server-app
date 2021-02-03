package at.tugraz.oop2.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DataSeriesJsonSerializer extends StdSerializer<DataSeries> {

    public DataSeriesJsonSerializer() {
        this(null);
    }

    public DataSeriesJsonSerializer(Class<DataSeries> t) {
        super(t);
    }

    @Override
    public void serialize(DataSeries dataPoints, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("sensor", dataPoints.getSensor());
        jsonGenerator.writeStringField("operation", dataPoints.getOperation().name());
        jsonGenerator.writeNumberField("interval", dataPoints.getInterval());
        jsonGenerator.writeArrayFieldStart("dataPoints");
        for (DataPoint dataPoint: dataPoints) {
            jsonGenerator.writeObject(dataPoint);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }
}