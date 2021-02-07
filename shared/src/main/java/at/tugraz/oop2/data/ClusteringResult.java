package at.tugraz.oop2.data;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ClusteringResult implements Serializable {
    private final Map<Integer, List<ClusterDescriptor>> trainingProgressClusters;
    private final List<ClusterDescriptor> finalClusters;

    public ClusteringResult()
    {
        this.trainingProgressClusters = new HashMap<>();
        this.finalClusters = new ArrayList<>();
    }
    public ClusteringResult(Map<Integer, List<ClusterDescriptor>> trainingProgressClusters, List<ClusterDescriptor> finalClusters) {
        this();
        this.trainingProgressClusters.putAll(trainingProgressClusters);
        this.finalClusters.addAll(finalClusters);
    }

    public void setFinalClusters(List<ClusterDescriptor> finalClusters)
    {
        this.finalClusters.clear();
        this.finalClusters.addAll(finalClusters);
    }

    public void addTrainingProgressCluster(int iteration, List<ClusterDescriptor> finalClusters)
    {
        this.trainingProgressClusters.put(iteration, finalClusters);
    }
}
