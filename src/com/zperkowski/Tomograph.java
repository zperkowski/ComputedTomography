package com.zperkowski;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

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
    private ArrayList<ArrayList<Integer>> hitsMatrix;

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
        hitsMatrix = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < pImage.getNormHeight(); i++) {
            hitsMatrix.add(new ArrayList<>());
            for (int j = 0; j < pImage.getNormHeight(); j++) {
                hitsMatrix.get(i).add(0);
            }
        }


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
                int gray = (int) (255 * sum);
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
                    }
                    ArrayList<ArrayList<Integer>> normHitsMatrix = normalize2DMatrix(hitsMatrix);
                    float normalized_value;
                    for (int y = 0; y < normHitsMatrix.size(); y++) {
                        for (int x = 0; x < normHitsMatrix.get(y).size(); x++) {
                            normalized_value = normHitsMatrix.get(y).get(x);
                            generatedImage.setRGB(x, y, ((int) normalized_value << 16) + ((int) normalized_value << 8) + (int) normalized_value);
                        }
                    }
                    canvasLeft.drawImage(SwingFXUtils.toFXImage(generatedImage, null), 0, 0,
                            generatedImage.getWidth(), generatedImage.getHeight());
                    return null;
                }
            };
            return task;
        }

        private void drawBeamOfRays(List<List<Map<String, Integer>>> rays, int rowOfSinogram) {
            for (int i = 0; i < rays.size(); i++) {
                // drawOneRay(rays.get(i), i, rowOfSinogram);
                drawOneRayOnHitsMatrix(rays.get(i), i, rowOfSinogram);
            }

        }

        private void drawOneRay(List<Map<String, Integer>> points, int columnOfSinogram,  int rowOfSinogram) {
            int mul, currentColor, x, y;
            java.awt.Color sinogramColor = new java.awt.Color(sinogram.getRGB(columnOfSinogram, rowOfSinogram));
            java.awt.Color imageColor;


            for (Map<String, Integer> point : points) {
                x = point.get("x");
                y = point.get("y");
                imageColor = new java.awt.Color(generatedImage.getRGB(x, y));

                if (sinogramColor.getRed() > 50)
                    mul = 2;
                else
                    mul = -1;
                currentColor = (int) (imageColor.getRed() + (255 * ((step / 180)) * mul));
                if (currentColor > 255)
                    currentColor = 255;
                if (currentColor < 0)
                    currentColor = 0;
                generatedImage.setRGB(x, y, (currentColor << 16) + (currentColor << 8) + currentColor);
            }
        }

        private Map<String, Integer> findMinAndMax(ArrayList<ArrayList<Integer>> matrix) {
            Map<String, Integer> map = new HashMap<>();
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int y = 0; y < matrix.size(); y++) {
                for (int x = 0; x < matrix.get(y).size(); x++) {
                    if (min > matrix.get(y).get(x))
                        min = matrix.get(y).get(x);
                    if (max < matrix.get(y).get(x))
                        max = matrix.get(y).get(x);
                }
            }
            map.put("min", min);
            map.put("max", max);
            return map;
        }

        private ArrayList<ArrayList<Integer>> normalize2DMatrix(ArrayList<ArrayList<Integer>> matrix) {
            ArrayList<ArrayList<Integer>> normMatrix = new ArrayList<ArrayList<Integer>>();
            Map<String, Integer> mapMinMax = findMinAndMax(matrix);
            double value, normalized_value;
            for (int y = 0; y < matrix.size() - 1; y++) {
                normMatrix.add(new ArrayList<>());
                for (int x = 0; x < matrix.get(y).size(); x++) {
                    value = (matrix.get(y).get(x));
                    normalized_value = (value - mapMinMax.get("min")) / (mapMinMax.get("max") - mapMinMax.get("min"));
                    normalized_value *= 255;
                    normMatrix.get(y).add((int) normalized_value);
                }
            }
            return normMatrix;
        }

        private void drawOneRayOnHitsMatrix(List<Map<String, Integer>> points, int columnOfSinogram,  int rowOfSinogram) {
            int x, y, currentColor;
            java.awt.Color sinogramColorObj = new java.awt.Color(sinogram.getRGB(columnOfSinogram, rowOfSinogram));
            int sinogramColorRed = sinogramColorObj.getRed();
            int sinogramColorGreen = sinogramColorObj.getGreen();
            int sinogramColorBlue = sinogramColorObj.getBlue();
            int sinogramColorGray = (sinogramColorRed + sinogramColorGreen + sinogramColorBlue) / 3;

            for (Map<String, Integer> point : points) {
                x = point.get("x");
                y = point.get("y");
                // Todo: Check if x and y are x and y
                currentColor = hitsMatrix.get(y).get(x);
                hitsMatrix.get(y).set(x, currentColor + sinogramColorGray);
            }
        }
    }
}
