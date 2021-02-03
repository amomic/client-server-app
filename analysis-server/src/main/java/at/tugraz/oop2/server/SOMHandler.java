package at.tugraz.oop2.server;

import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.ClusterDescriptor;
import at.tugraz.oop2.data.DataSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public final class SOMHandler {
    private final int height;
    private final int width;
    private int length;
    private List<DataSeries> data;

    private final int maxIterationCount;
    private int iteration;
    private double learningRate;
    private double updateRadius;

    private int intermediatePlots;

    private double updateRadiusDecayRate;
    private double learningRateDecayRate;

    private double currentLearningRate;
    private double currentUpdateRadius;

    private double[][][] weights;
    private ClusterDescriptor[][] clusters;

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

        this.length = data.get(0).size();
        this.weights = new double[this.height][this.width][this.length];

        this.updateRadiusDecayRate = 0.1;
        this.learningRateDecayRate = 0.1;

        this.currentLearningRate = learningRate;
        this.currentUpdateRadius = learningRate;

    }


    public SOMHandler(List<DataSeries> data, int height, int width, int length,
                      double learningRate, double updateRadius, int maxIterationCount,
                      int intermediatePlots, double updateRadiusDecayRate, double learningRateDecayRate) {
        this(data, height, width, learningRate, updateRadius, maxIterationCount, intermediatePlots);
        this.length = length;
        this.updateRadiusDecayRate = updateRadiusDecayRate;
        this.learningRateDecayRate = learningRateDecayRate;
    }


    public void run()
    {
        this.initWeights();
        this.train();
        this.initClusters();
        this.clusterMembers();
    }


    private void initClusters() {
        this.clusters = new ClusterDescriptor[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                List<Double> weights = DoubleStream.of(this.weights[i][j]).boxed().collect(Collectors.toList());
                this.clusters[i][j] = new ClusterDescriptor(i, j, weights);
            }
        }
    }


    private void clusterMembers()
    {
        for (DataSeries sample : this.data)
        {
            int[] clusterPos = this.findBestMatchingNeuron(sample);
            this.clusters[clusterPos[0]][clusterPos[1]].addMember(sample);
        }
    }


    public List<ClusterDescriptor> getClusters()
    {
        List<ClusterDescriptor> clusters = new ArrayList<>();
        for (int i = 0; i < this.height; i++) {
            clusters.addAll(Arrays.asList(this.clusters[i]));
        }
        return clusters;
    }


    private void initWeights() {
        Random random = new Random();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                for (int p = 0; p < this.length; p++) {
                    this.weights[i][j][p] = random.nextDouble();
                }
            }
        }
    }


    private void train()
    {
        for (int t = 0; t < this.maxIterationCount; t++)
        {
            // adjust update radius and learning rate for each step
            // decay function is time decay: meaning the radius and learning rate will be X% less with every iteration
            this.iteration = t;
            this.currentUpdateRadius *= 1 / (1 + this.updateRadiusDecayRate * this.iteration);
            this.currentLearningRate *= 1 / (1 + this.learningRateDecayRate * this.iteration);

            for (DataSeries sample : this.data) {

                // find best matching neuron (winner neuron) position
                int[] winnerPos = this.findBestMatchingNeuron(sample);

                // get neighbourhood influence mask
                boolean[][] mask = this.getNeighbourhoodInfluenceMask(winnerPos);

                // update weights
                this.updateWeights(winnerPos, mask, sample.getValues());
            }
        }
    }


    private int[] findBestMatchingNeuron(DataSeries input)
    {
        int[] bestPos = {0, 0};
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                double distance = DistanceMetrics.euclidean(this.weights[i][j], input.getValues());
                if (distance < bestDistance) {
                    bestPos[0] = i;
                    bestPos[1] = j;
                    bestDistance = distance;
                }
            }
        }
        return bestPos;
    }


    private void updateWeights(int[] winnerPos, boolean[][] mask, double[] input) {

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {

                // don't update elements that outside of the neighbourhood
                if (!mask[i][j]) {
                    continue;
                }

                // find distance between neighbouring neuron and winner neuron
                int[] neighbourPos = {i, j};
                double distance = DistanceMetrics.euclidean(winnerPos, neighbourPos);

                //  subtract vectors: diff = || xi(t) - mi(t) ||
                double[] diff = Util.subtractVectors(input, this.weights[i][j]);

                // update neuron for the next iteration: mi(t+1) = mi(t) + hci(t) * diff
                for (int p = 0; p < this.length; p++)
                {
                    this.weights[i][j][p] +=
                            NeighbourhoodKernels.gaussian(this.currentLearningRate, distance, this.currentUpdateRadius)
                            * diff[p];
                }
            }
        }
    }


    private boolean[][] getNeighbourhoodInfluenceMask(int[] winnerPos)
    {
        boolean[][] mask = new boolean[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int[] potentialNeighbourPos = {i, j};
                double distance = DistanceMetrics.euclidean(winnerPos, potentialNeighbourPos);
                mask[i][j] = distance < this.currentUpdateRadius;
            }
        }
        return mask;
    }


}
