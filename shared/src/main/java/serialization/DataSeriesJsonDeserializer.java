package at.tugraz.oop2.serialization;

import at.tugraz.oop2.data.DataPoint;
import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.Sensor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DataSeriesJsonDeserializer extends StdDeserializer<DataSeries> {

    public DataSeriesJsonDeserializer() {
        this(null);
    }

    public DataSeriesJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DataSeries deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        Sensor sensor = mapper.readValue(node.get("sensor").toString(), Sensor.class);
        DataSeries.Operation operation = mapper.readValue(node.get("operation").toString(), DataSeries.Operation.class);
        DataPoint[] dataPoints = mapper.readValue(node.get("dataPoints").toString(), DataPoint[].class);
        int interval = node.get("interval").numberValue().intValue();
        DataSeries deserialized = new DataSeries(sensor, interval, operation);
        deserialized.addAll(Arrays.asList(dataPoints));
        return deserialized;
    }
}
