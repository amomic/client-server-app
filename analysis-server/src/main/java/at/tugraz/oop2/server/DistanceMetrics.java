package at.tugraz.oop2.server;


public class DistanceMetrics {

    public static double euclidean(double[] vectorA, double[] vectorB)
    {
        if (vectorA.length != vectorB.length)
            throw new IllegalArgumentException("Vectors must be the same size!");
        double squareSum = 0;
        for (int i = 0; i < vectorA.length; i++)
        {
            squareSum += Math.pow(vectorA[i] - vectorB[i], 2);
        }
        return Math.sqrt(squareSum);
    }


    public static double euclidean(int[] vectorA, int[] vectorB)
    {
        if (vectorA.length != vectorB.length)
            throw new IllegalArgumentException("Vectors must be the same size!");
        double squareSum = 0;
        for (int i = 0; i < vectorA.length; i++)
        {
            squareSum += Math.pow(vectorA[i] - vectorB[i], 2);
        }
        return Math.sqrt(squareSum);
    }
}
