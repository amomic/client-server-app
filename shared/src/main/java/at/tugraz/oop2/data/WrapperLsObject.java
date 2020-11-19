package at.tugraz.oop2.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class WrapperLsObject implements Serializable {
    private List<Sensor> sensorList;
}
