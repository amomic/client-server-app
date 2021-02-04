package at.tugraz.oop2.server;

import at.tugraz.oop2.Util;
import at.tugraz.oop2.data.ClusterDescriptor;
import at.tugraz.oop2.data.DataSeries;

import java.util.*;
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
    private double gridDiameter;

    private int intermediatePlots;

    private double updateRadiusDecayRate;
    private double learningRateDecayRate;

    private double currentLearningRate;
    private double currentUpdateRadius;

    private double[][][] weights;
    private ClusterDescriptor[][] clusters;

    private Map<Integer, ClusterDescriptor[][]> progress;


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

        this.updateRadiusDecayRate = updateRadius / maxIterationCount;
        this.learningRateDecayRate = learningRate / maxIterationCount;
        this.gridDiameter = Math.sqrt(Math.pow(height - 1, 2) + Math.pow(width - 1, 2));

        this.currentLearningRate = learningRate;
        this.currentUpdateRadius = updateRadius;

        this.progress = new HashMap<>();
    }


    public SOMHandler(List<DataSeries> data, int height, int width, int length,
                      double learningRate, double updateRadius, int maxIterationCount,
                      int intermediatePlots) {
        this(data, height, width, learningRate, updateRadius, maxIterationCount, intermediatePlots);
        this.length = length;
    }


    public void run() {
        this.initWeights();
        this.train();
        this.clusters = this.initClusters(true);
        this.clusterMembers(this.clusters);
    }


    private void initWeights() {
        Random random = new Random();
        double rangeMin = 0;
        double rangeMax = 1;
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                for (int p = 0; p < this.length; p++) {
                    this.weights[i][j][p] = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
                }
            }
        }
    }


    private void train() {
        for (int t = 0; t < this.maxIterationCount; t++) {
            // adjust update radius and learning rate for each step
            // decay function is time decay: meaning the radius and learning rate will be X% less with every iteration
            this.iteration = t;
            this.currentUpdateRadius = this.updateRadius * 1 / (1 + this.updateRadiusDecayRate * this.iteration);
            this.currentLearningRate = this.learningRate * 1 / (1 + this.learningRateDecayRate * this.iteration);

            for (DataSeries sample : this.data) {

                // find best matching neuron (winner neuron) position
                int[] winnerPos = this.findBestMatchingNeuron(sample);

                // get neighbourhood influence mask
                boolean[][] mask = this.getNeighbourhoodInfluenceMask(winnerPos);

                // update weights
                this.updateWeights(mask, sample.getValues());
            }

            // record progress every Nth step so we have this.intermediatePlots number of records
            if (this.iteration % (this.maxIterationCount / this.intermediatePlots) == 0) {
                this.recordProgress(t);
            }
        }
    }


    private int[] findBestMatchingNeuron(DataSeries input) {
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


    private boolean[][] getNeighbourhoodInfluenceMask(int[] winnerPos) {
        boolean[][] mask = new boolean[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                int[] potentialNeighbourPos = {i, j};
                if (potentialNeighbourPos == winnerPos) {
                    mask[i][j] = true;
                } else {
                    double distance = DistanceMetrics.euclidean(winnerPos, potentialNeighbourPos);
                    double updateDistance = this.gridDiameter * this.currentUpdateRadius;
                    mask[i][j] = distance <= updateDistance;
                }
            }
        }
        return mask;
    }


    private void updateWeights(boolean[][] mask, double[] input) {

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {

                // don't update elements that outside of the neighbourhood
                if (!mask[i][j]) {
                    continue;
                }

                //  subtract vectors: diff = || xi(t) - mi(t) ||
                double[] diff = Util.subtractVectors(input, this.weights[i][j]);

                // update neuron for the next iteration: mi(t+1) = mi(t) + hci(t) * diff
                for (int p = 0; p < this.length; p++) {
                    this.weights[i][j][p] +=
                            NeighbourhoodKernels.bubble(this.currentLearningRate)
                                    * diff[p];
                }
            }
        }
    }


    private void recordProgress(int iteration) {
        ClusterDescriptor[][] clusters = this.initClusters(false);
        this.clusterMembers(clusters);
        this.progress.put(iteration, clusters);
    }


    private ClusterDescriptor[][] initClusters(boolean finished) {
        ClusterDescriptor[][] clusters = new ClusterDescriptor[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                List<Double> weights = DoubleStream.of(this.weights[i][j]).boxed().collect(Collectors.toList());
                clusters[i][j] = new ClusterDescriptor(i, j, weights);
                clusters[i][j].setFinished(finished);
            }
        }
        return clusters;
    }


    private void clusterMembers(ClusterDescriptor[][] clusters) {
        for (DataSeries sample : this.data) {
            int[] clusterPos = this.findBestMatchingNeuron(sample);
            clusters[clusterPos[0]][clusterPos[1]].addMember(sample);
        }
    }


    public List<ClusterDescriptor> getClusters() {
        List<ClusterDescriptor> clusters = new ArrayList<>();
        for (int i = 0; i < this.height; i++) {
            clusters.addAll(Arrays.asList(this.clusters[i]));
        }
        return clusters;
    }


    public Map<Integer, List<ClusterDescriptor>> getTrainingProgressClusters() {
        Map<Integer, List<ClusterDescriptor>> progress = new HashMap<>();
        for (Map.Entry<Integer, ClusterDescriptor[][]> entry : this.progress.entrySet()) {
            List<ClusterDescriptor> clusters = new ArrayList<>();
            for (int i = 0; i < this.height; i++) {
                clusters.addAll(Arrays.asList(entry.getValue()[i]));
            }
            progress.put(entry.getKey(), clusters);
        }
        return progress;
    }

}
