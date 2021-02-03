package at.tugraz.oop2.server;

public class NeighbourhoodKernels {


    public static double bubble(double learningRate) {
        return learningRate;
    }

    public static double gaussian(double learningRate, double distance, double updateRadius) {
        return learningRate * Math.exp(-Math.pow(distance,2) / 2*Math.pow(updateRadius,2));
    }
}
