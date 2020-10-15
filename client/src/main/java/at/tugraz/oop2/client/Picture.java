package at.tugraz.oop2.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Picture {

    private final int width;
    private final int height;

    private final BufferedImage bufferedImage;
    private final Graphics2D graphics2D;

    public Picture(int width, int height) {
        this.width = width;
        this.height = height;
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }

    public void save(String path) throws IOException {
        File file = new File(path);
        ImageIO.write(bufferedImage, "png", file);
    }
}
