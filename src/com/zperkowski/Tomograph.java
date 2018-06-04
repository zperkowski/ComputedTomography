package com.zperkowski;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class Tomograph {

    private ProcessingImage pImage;
    private double rays, angle, step;
    private BufferedImage sinogram;
    private GraphicsContext canvas;

    private TaskGenerateSinogram sinogramGenerator;

    public Tomograph(ProcessingImage pImage, double rays, double angle, double step, GraphicsContext canvasToShow) {
        this.pImage = pImage;
        this.rays = rays;
        this.angle = angle;
        this.step = step;
        this.canvas = canvasToShow;

        int canvasWidth = (int) rays;
        int canvasHeight = (int) Math.ceil(360 / step);

        this.canvas.getCanvas().setWidth(canvasWidth);
        this.canvas.getCanvas().setHeight(canvasHeight);
        sinogram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_BYTE_GRAY);
        System.out.println("Sinogram: " + sinogram);
    }

    public TaskGenerateSinogram getSinogramGenerator() {
        if (sinogramGenerator == null)
            sinogramGenerator = new TaskGenerateSinogram();
        return sinogramGenerator;
    }


    class TaskGenerateSinogram extends Service<Void> {
        int rowOfSinogram = 0;

        @Override
        protected Task<Void> createTask() {
            Task task = new Task<Double>() {
                protected Double call() throws Exception {
                    List<List> lines;
                    for (int i = 0; i < 360; i += step) {
                        Thread.sleep(75);
                        System.out.println(i + "\t" + ((double) i / 359));
                        pImage.drawRays(rays, (double) i, angle);
                        lines = pImage.bresenham(rays, (double) i, angle);
                        generateSinogram(lines, rowOfSinogram);
                        updateProgress((i / 359), 1.0);
                        canvas.drawImage(SwingFXUtils.toFXImage(sinogram, null), 0, 0,
                                                            sinogram.getWidth(), sinogram.getHeight());
                        rowOfSinogram++;
                    }
                    updateProgress(1.0, 1.0);
                    return 1.0;
                }
            };
            return task;
        }

        private void generateSinogram(List<List> lines, int rowOfSinogram) {
            for (int i = 0; i < lines.size(); i++) {
                List<Map<String, Integer>> pointsOfLine = lines.get(i);
                double sum = 0.0;
                int x, y, j;
                for (j = 0; j < pointsOfLine.size(); j++) {
                    x = pointsOfLine.get(j).get("x");
                    y = pointsOfLine.get(j).get("y");
                    sum += RGBtoGray(pImage.getImageGray().getPixelReader().getColor(x,y));
                }
                sum /= j;
                int gray = (int) (255 / sum);
                sinogram.setRGB(i, rowOfSinogram, (gray << 16) + (gray << 8) + gray);
            }
        }

        private double RGBtoGray(Color rgb) {
            double r = rgb.getRed();
            double g = rgb.getGreen();
            double b = rgb.getBlue();

            double gray = (r + g + b) / 3;
            return gray;
        }
    }

}
