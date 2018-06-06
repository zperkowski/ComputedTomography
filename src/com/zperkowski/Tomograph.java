package com.zperkowski;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Tomograph {

    private ProcessingImage pImage;
    private double rays, angle, step;
    private BufferedImage sinogram;
    private GraphicsContext canvasRight;
    private GraphicsContext canvasLeft;
    private List<List> allCalculatedLines;
    private BufferedImage generatedImage;

    private TaskGenerateSinogram sinogramGenerator;
    private TaskGenerateImage generateImage;

    public Tomograph(ProcessingImage pImage, double rays, double angle, double step, GraphicsContext canvasLeft, GraphicsContext canvasRight) {
        this.pImage = pImage;
        this.rays = rays;
        this.angle = angle;
        this.step = step;
        this.canvasLeft = canvasLeft;
        this.canvasRight = canvasRight;

        int canvasWidth = (int) rays;
        int canvasHeight = (int) Math.ceil(360 / step);

        this.canvasRight.getCanvas().setWidth(canvasWidth);
        this.canvasRight.getCanvas().setHeight(canvasHeight);
        sinogram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_BYTE_GRAY);
        System.out.println("Sinogram: " + sinogram);

        allCalculatedLines = new ArrayList<>();
        generatedImage = new BufferedImage(pImage.getNormWidth(), pImage.getNormHeight(), BufferedImage.TYPE_BYTE_GRAY);
    }

    public TaskGenerateSinogram getSinogramGenerator() {
        if (sinogramGenerator == null)
            sinogramGenerator = new TaskGenerateSinogram();
        return sinogramGenerator;
    }

    public TaskGenerateImage getImageGenerator() {
        if (generateImage == null)
            generateImage = new TaskGenerateImage();
        return generateImage;
    }

    private double RGBtoGray(Color rgb) {
        double r = rgb.getRed();
        double g = rgb.getGreen();
        double b = rgb.getBlue();

        double gray = (r + g + b) / 3;
        return gray;
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
                        pImage.reloadCanvas();
                        pImage.drawOval();
                        pImage.drawRays(rays, (double) i, angle);
                        lines = pImage.bresenham(rays, (double) i, angle);
                        allCalculatedLines.add(lines);
                        generateSinogram(lines, rowOfSinogram);
                        updateProgress((i / 359), 1.0);
                        canvasRight.drawImage(SwingFXUtils.toFXImage(sinogram, null), 0, 0,
                                                            sinogram.getWidth(), sinogram.getHeight());
                        rowOfSinogram++;
                    }
                    updateProgress(1.0, 1.0);
                    getImageGenerator().start();
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
                int gray;
                if (sum != 0.0)
                    gray = (int) (255 / sum);
                else
                    gray = 0;
                sinogram.setRGB(i, rowOfSinogram, (gray << 16) + (gray << 8) + gray);
            }
        }
    }

    class TaskGenerateImage extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    System.out.println("TaskGenerateImage");
                    canvasLeft.getCanvas().setWidth(generatedImage.getWidth());
                    canvasLeft.getCanvas().setHeight(generatedImage.getHeight());
                    for (int i = 0; i < allCalculatedLines.size(); i++) {
                        System.out.println(i + "\t" + ((double) i / 359));
                        drawBeamOfRays(allCalculatedLines.get(i), i);
                        canvasLeft.drawImage(SwingFXUtils.toFXImage(generatedImage, null), 0, 0,
                                                generatedImage.getWidth(), generatedImage.getHeight());
                    }
                    return null;
                }
            };
            return task;
        }

        private void drawBeamOfRays(List<List<Map<String, Integer>>> rays, int rowOfSinogram) {
            for (int i = 0; i < rays.size(); i++) {
                drawOneRay(rays.get(i), i, rowOfSinogram);
            }

        }

        private void drawOneRay(List<Map<String, Integer>> points, int columnOfSinogram,  int rowOfSinogram) {
            int color, currentColor, x, y;
            java.awt.Color sinogramColor = new java.awt.Color(sinogram.getRGB(columnOfSinogram, rowOfSinogram));
            java.awt.Color imageColor;


            for (Map<String, Integer> point : points) {
                x = point.get("x");
                y = point.get("y");
                imageColor = new java.awt.Color(generatedImage.getRGB(x, y));
                currentColor = ((imageColor.getRed() +  sinogramColor.getRed()) / 2);
                generatedImage.setRGB(x, y, (currentColor << 16) + (currentColor << 8) + currentColor);
            }
        }
    }
}
