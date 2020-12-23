package at.tugraz.oop2.server;

import java.util.ArrayList;
import java.util.List;

import at.tugraz.oop2.data.DataSeries;
import at.tugraz.oop2.data.DataPoint;
import at.tugraz.oop2.data.ClusterDescriptor;

public final class SOMHandler {
    private final int height;
    private final int width;
    private List<DataSeries> data;

    private final int maxIterationCount;
    private int iteration;
    private double learningRate;
    private double updateRadius;

    private int intermediatePlots;

    public SOMHandler(List<DataSeries> data, int height, int width,
                      double learningRate, double updateRadius, int maxIterationCount,
                      int intermediatePlots) {
        this.data = data;

        this.height = height;
        this.width = width;

        this.learningRate = learningRate;
        this.updateRadius = updateRadius;

        this.iteration = 0;
        this.maxIterationCount = maxIterationCount;
        this.intermediatePlots = intermediatePlots;
    }
}
