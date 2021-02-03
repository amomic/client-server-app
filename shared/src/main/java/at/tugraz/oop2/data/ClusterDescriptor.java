package at.tugraz.oop2.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a cluster returned by the SOM algorithm. It contains a weights vector along with its
 * existing member data series.
 */
@EqualsAndHashCode
@Data
public final class ClusterDescriptor implements Serializable {
    private final int heigthIndex;
    private final int widthIndex;
    private final List<Double> weights;
    private final List<DataSeries> members;
    final double updateRadius = 0;
    boolean finished = false;

    private Double error = -1.d;
    private Double normalizedError = -1.d;
    private Double normalizedAmountOfMembers = -1.d;
    private Double distanceEntropy = -1.d;
    private Double normalizedDistanceEntropy = -1.d;

    public ClusterDescriptor(int heigthIndex, int widthIndex, List<Double> weights) {
        this.heigthIndex = heigthIndex;
        this.widthIndex = widthIndex;
        this.weights = weights;
        this.members = new ArrayList<>();
    }


    public void addMember(DataSeries member) {
        this.members.add(member);
    }


    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
