package at.tugraz.oop2.client;

import at.tugraz.oop2.data.ClusterDescriptor;
import at.tugraz.oop2.data.ClusteringResult;
import at.tugraz.oop2.data.DataPoint;
import at.tugraz.oop2.data.DataSeries;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// source -> https://github.com/Madonahs/Graphics-2D-in-Java/blob/master/SimpleGraph/SampleGraph/src/com/madonasyombua/exe/Graph.java

public class ClusterLineChart {

    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int width = 200;
    private int height = 100;
    private final BufferedImage bufferedImage;
    private final Graphics2D graphics2D;
    private final int padding = 10;
    private final int labelPadding = 25;
    private final Color lineColor = new Color(10, 255, 10, 255);
    private final Color pointColor = new Color(44, 102, 230, 180);
    private final int pointWidth = 4;
    private int x_offset = 0;
    private int y_offset = 0;
    private int inner_plot_width = 0;
    private int inner_plot_height = 0;
    private int outer_plot_width = 3000;
    private int outer_plot_height = 3000;

    private List<ClusterDescriptor> cluster;
    private List<Double> dataPoints;
    private List<DataPoint> listNumbers;
    private List<DataSeries> listSeries;

    public ClusterLineChart(List<ClusterDescriptor> results, int plotheight, int plotwidth) {

        this.cluster = results;
        this.x_offset = 0;
        this.y_offset = 0;
        this.inner_plot_height = plotheight;
        this.inner_plot_width = plotwidth;
        this.width = inner_plot_width;
        this.height = inner_plot_height;
        this.outer_plot_height = 3000;
        this.outer_plot_width = 3000;

        bufferedImage = new BufferedImage(outer_plot_width, outer_plot_height, BufferedImage.TYPE_INT_RGB);
        graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }

    public void save(String path) throws IOException {
        File file = new File(path);
        x_offset += 200;
        y_offset += 100;

        graphics2D.drawImage(bufferedImage, 200 + x_offset, 100 + y_offset, null);
        ImageIO.write(bufferedImage, "png", file);
    }

    public void saveNew(String path) throws IOException {
        File file = new File(path);
        ImageIO.write(bufferedImage, "png", file);
    }

    public void storeImage(int heightidx, int widthidx, BufferedImage bi) {
        x_offset = 0;
        y_offset = 0;
        if(heightidx != 0)
            y_offset = inner_plot_height + (heightidx * inner_plot_height);

        if(widthidx != 0)
            x_offset = inner_plot_width + (widthidx * inner_plot_width);
        graphics2D.drawImage(bi, x_offset, y_offset, null);
    }

    public void run(){
        int i = 0;
        for(ClusterDescriptor cd : cluster){
            this.dataPoints = cd.getWeights();
            this.listSeries = cd.getMembers();
            BufferedImage bi = new BufferedImage(inner_plot_width, inner_plot_height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            newPaint(cd, g2d);
            storeImage(cd.getHeigthIndex()+1, cd.getWidthIndex()+1, bi);
        }
    }

    void newPaint(ClusterDescriptor cd, Graphics2D g2d){
        dataPoints = cd.getWeights();
        listSeries = cd.getMembers();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double xScale = ((double) width - (2 * padding)) / (dataPoints.size() - 1);
        double yScale = ((double) height - 2 * padding ) / (getMaxScore() - getMinScore());

        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < dataPoints.size(); i++) {
            int x1 = (int) (i * xScale + padding );
            int y1 = (int) ((getMaxScore() - dataPoints.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        // create x and y axes
        g2d.drawLine(padding , height - padding, padding , padding);
        g2d.drawLine(padding, height - padding , width - padding, height - padding );

        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(lineColor);
        g2d.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(oldStroke);
        g2d.setColor(pointColor);

        for(DataSeries series: listSeries)
        {
            yScale = ((double) height - 2 * padding ) / ((getMaxDataPoints(series)) - (getMinDataPoints(series)));

            g2d.setColor(new Color(10, 102, 230, 200));
            g2d.setStroke(new BasicStroke(0.5f));
            int i = 0;
            List<Point> graphPoints2 = new ArrayList<>();
            for (DataPoint point : series) {
                int x1 = (int) (i * xScale + padding );
                int y1 = (int) ((getMaxDataPoints(series) - point.getValue()) * yScale + padding);
                graphPoints2.add(new Point(x1, y1));
                i++;
            }


            for (i = 0; i < graphPoints2.size() - 1; i++) {
                int x1 = graphPoints2.get(i).x;
                int y1 = graphPoints2.get(i).y;
                int x2 = graphPoints2.get(i + 1).x;
                int y2 = graphPoints2.get(i + 1).y;
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

    }

    void paintComponent() {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double xScale = ((double) width - (2 * padding)) / (dataPoints.size() - 1);
        double yScale = ((double) height - 2 * padding ) / (getMaxScore() - getMinScore());
        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < dataPoints.size(); i++) {
            int x1 = (int) (i * xScale + padding );
            int y1 = (int) ((getMaxScore() - dataPoints.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        // create x and y axes
        graphics2D.drawLine(padding , height - padding, padding , padding);
        graphics2D.drawLine(padding, height - padding , width - padding, height - padding );

        //white background
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, width, height);

        Stroke oldStroke = graphics2D.getStroke();
        graphics2D.setColor(lineColor);
        graphics2D.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            graphics2D.drawLine(x1, y1, x2, y2);
        }

        graphics2D.setStroke(oldStroke);
        graphics2D.setColor(pointColor);

        for(DataSeries series: listSeries)
        {
            yScale = ((double) height - 2 * padding ) / ((getMaxDataPoints(series)) - (getMinDataPoints(series)));
            graphics2D.setColor(new Color(10, 102, 230, 200));
            graphics2D.setStroke(new BasicStroke(0.5f));
            int i = 0;
            List<Point> graphPoints2 = new ArrayList<>();
            for (DataPoint point : series) {
                int x1 = (int) (i * xScale + padding );
                int y1 = (int) ((getMaxDataPoints(series) - point.getValue()) * yScale + padding);
                graphPoints2.add(new Point(x1, y1));
                i++;
            }


            for (i = 0; i < graphPoints2.size() - 1; i++) {
                int x1 = graphPoints2.get(i).x;
                int y1 = graphPoints2.get(i).y;
                int x2 = graphPoints2.get(i + 1).x;
                int y2 = graphPoints2.get(i + 1).y;
                graphics2D.drawLine(x1, y1, x2, y2);

            }
        }

    }

    private double getMinDataPoints(DataSeries score) {
        double minScore = Double.MAX_VALUE;

        for(DataPoint second_score : score)
        {
            minScore = Math.min(minScore, second_score.getValue());
        }
        return minScore;
    }

    private double getMaxDataPoints(DataSeries score) {
        double maxScore = Double.MIN_VALUE;
        for (DataPoint second_score : score) {
            maxScore = Math.max(maxScore, second_score.getValue());
        }
        return maxScore;
    }


    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (Double score : dataPoints) {
            minScore = Math.min(minScore, score);
        }
        return minScore;
    }

    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (Double score : dataPoints) {
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }

    public void setScores(List<Double> dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void createDataPoints() {
        List<Double> dataPoints = new ArrayList<>();
        int maxScore = 10;
        for (int i = 0; i < listNumbers.size(); i++) {
            DataPoint dp = listNumbers.get(i);
            dataPoints.add(dp.getValue() * maxScore);
        }
        setScores(dataPoints);
    }
}
