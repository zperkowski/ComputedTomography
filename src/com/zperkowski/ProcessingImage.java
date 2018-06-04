package com.zperkowski;

import com.sun.istack.internal.Nullable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.*;

public class ProcessingImage {
    private Image image;
    private Image imageGray;
    private BufferedImage bufferedImage;
    private BufferedImage bufferedImageGray;
    private GraphicsContext graphicsContext;

    private int centerX, centerY;
    private int width, height;
    private int normWidth, normHeight;  // Normalized dimensions

    public ProcessingImage(Image image, Canvas canvas) {
        this.image = image;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.bufferedImage = SwingFXUtils.fromFXImage(this.image, null);

        width = (int) image.getWidth();
        height = (int) image.getHeight();

        changeCanvasSize(this.image, canvas);
        reloadCanvas();

        centerX = width / 2;
        centerY = height / 2;

        if (height > width)
            normHeight = normWidth = width;
        else
            normHeight = normWidth = height;
    }


    public Image getImage() {
        return image;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNormWidth() {
        return normWidth;
    }

    public int getNormHeight() {
        return normHeight;
    }

    public void clearCanvas() {
        graphicsContext.clearRect(0, 0, width, height);
        changeCanvasSize(null, graphicsContext.getCanvas());
    }

    public void reloadCanvas() {
        graphicsContext.drawImage(image, 0, 0, width, height);
    }

    private void changeCanvasSize(@Nullable Image image, Canvas canvas) {
        if (image == null) {
            canvas.setHeight(0);
            canvas.setWidth(0);
        } else {
            canvas.setWidth(image.getWidth());
            canvas.setHeight(image.getHeight());
        }
    }

    public Image getImageGray() {
        if (imageGray == null) {
            imageGray = SwingFXUtils.toFXImage(getBufferedImageGray(), null);
        }
        return imageGray;
    }

    public BufferedImage getBufferedImageGray() {
        if (bufferedImageGray == null) {
            bufferedImageGray = SwingFXUtils.fromFXImage(image, null);
            bufferedImageGray = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(bufferedImageGray, null);
        }
        return bufferedImageGray;
    }

    public void drawOval() {
        graphicsContext.setStroke(Color.RED);
        graphicsContext.strokeOval((getCenterX() - getNormWidth() / 2),
                (getCenterY() - getNormHeight() / 2),
                getNormWidth(),
                getNormHeight());
    }

    public List<Map> getPoints(double rays, double step, double angle) {
        List<Map> listMaps = new ArrayList<>();

        if (rays == 1)
            angle = 0;
        else
            angle *= 2; // Inscribed angle

        double xStart, yStart, xEnd, yEnd;
        double radAlpha = step * Math.PI / 180;
        double radBeta = angle * Math.PI / 180;
        xStart = (getCenterX() + Math.sin(radAlpha) * ((getNormWidth() / 2) - 1));
        yStart = (getCenterY() + Math.cos(radAlpha) * ((getNormHeight() / 2) - 1));

        for (int i = 0; i < rays; i++) {
            if (rays == 1)
                rays = 0;   // To avoid calculating Not a Number
            double radGamma = radAlpha + Math.PI - (radBeta / 2) + (i / (rays - 1) * radBeta);
            xEnd = (getCenterX() + Math.sin(radGamma) * ((getNormWidth() / 2) - 1));
            yEnd = (getCenterY() + Math.cos(radGamma) * ((getNormHeight() / 2) - 1));

            Map<String, Double> mapPoints = new HashMap<String, Double>();
            mapPoints.put("yEnd", yEnd);
            mapPoints.put("xEnd", xEnd);
            mapPoints.put("yStart", yStart);
            mapPoints.put("xStart", xStart);

            listMaps.add(mapPoints);
        }
        return listMaps;
    }

    public void drawRays(double rays, double step, double angle) {
        graphicsContext.setStroke(Color.RED);
        double xStart, yStart, xEnd, yEnd;

        List<Map> listMaps = getPoints(rays, step, angle);

        for (Map<String, Double> line : listMaps) {
            xStart = line.get("xStart");
            yStart = line.get("yStart");
            xEnd = line.get("xEnd");
            yEnd = line.get("yEnd");
            graphicsContext.strokeLine(xStart, yStart, xEnd, yEnd);
        }
    }

    public List<Map> pointsToInt(double rays, double step, double angle) {
        List<Map> listInts = new ArrayList<>();
        double xStart, yStart, xEnd, yEnd;
        Integer xStartInt, yStartInt, xEndInt, yEndInt;

        List<Map> listMaps = getPoints(rays, step, angle);

        for (Map<String, Double> line : listMaps) {
            xStart = line.get("xStart");
            yStart = line.get("yStart");
            xEnd = line.get("xEnd");
            yEnd = line.get("yEnd");

            Map<String, Integer> normalizedPoints = normalizePoints(xStart, yStart);
            xStartInt = normalizedPoints.get("x");
            yStartInt = normalizedPoints.get("y");
            normalizedPoints = normalizePoints(xEnd, yEnd);
            xEndInt = normalizedPoints.get("x");
            yEndInt = normalizedPoints.get("y");

            Map<String, Integer> mapInts = new HashMap<String, Integer>();
            mapInts.put("xStart", xStartInt);
            mapInts.put("yStart", yStartInt);
            mapInts.put("xEnd", xEndInt);
            mapInts.put("yEnd", yEndInt);

            listInts.add(mapInts);
        }
        return listInts;
    }

    private Map<String, Integer> normalizePoints(Double x, Double y) {
        // Assume that the points are in the middle of the image so they are ints
        Integer normalizedX = x.intValue();
        Integer normalizedY = y.intValue();

        if (x > getCenterX()) {
            if (y > getCenterY()) {
                normalizedX = (int) Math.floor(x);
                normalizedY = (int) Math.floor(y);
            } else if (y < getCenterY()) {
                normalizedX = (int) Math.floor(x);
                normalizedY = (int) Math.ceil(y);
            }
        } else if (x < getCenterX()) {
            if (y > getCenterY()) {
                normalizedX = (int) Math.ceil(x);
                normalizedY = (int) Math.floor(y);
            } else if (y < getCenterY()) {
                normalizedX = (int) Math.ceil(x);
                normalizedY = (int) Math.ceil(y);
            }
        }
        Map<String, Integer> normalizedPoints = new HashMap<String, Integer>();
        normalizedPoints.put("x", normalizedX);
        normalizedPoints.put("y", normalizedY);
        return normalizedPoints;
    }

    public List<List> bresenham(double rays, double step, double angle) {
        int xStart, yStart, xEnd, yEnd;
        List<Map> listPointsToCalculate = pointsToInt(rays, step, angle);
        List<Map> listPointsOfLine;
        List<List> listAllLines = new ArrayList<>();
        Map<String, Integer> mapXY;

        for (Map<String, Integer> pointsToCalculate : listPointsToCalculate) {
            listPointsOfLine = new ArrayList<>();
            xStart = pointsToCalculate.get("xStart");
            yStart = pointsToCalculate.get("yStart");
            xEnd = pointsToCalculate.get("xEnd");
            yEnd = pointsToCalculate.get("yEnd");

            int xShift = xStart - xEnd;
            int yShift = yStart - yEnd;

            int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
            if (xShift < 0) dx1 = -1;
            else if (xShift > 0) dx1 = 1;
            if (yShift < 0) dy1 = -1;
            else if (yShift > 0) dy1 = 1;
            if (xShift < 0) dx2 = -1;
            else if (xShift > 0) dx2 = 1;
            int longest = Math.abs(xShift);
            int shortest = Math.abs(yShift);
            if (!(longest > shortest)) {
                longest = Math.abs(yShift);
                shortest = Math.abs(xShift);
                if (yShift < 0) dy2 = -1;
                else if (yShift > 0) dy2 = 1;
                dx2 = 0;
            }
            int numerator = longest >> 1;
            for (int i = 0; i <= longest; i++) {
                mapXY = new HashMap<>();
                mapXY.put("x", xEnd);
                mapXY.put("y", yEnd);
                // Adding at the begin because points are calculated from the end
                listPointsOfLine.add(0, mapXY);
                numerator += shortest;
                if (!(numerator < longest)) {
                    numerator -= longest;
                    xEnd += dx1;
                    yEnd += dy1;
                } else {
                    xEnd += dx2;
                    yEnd += dy2;
                }
            }
            listAllLines.add(listPointsOfLine);
//            Commented to speed up
//            for (Map point :
//                    listPointsOfLine) {
//                System.out.print(point.get("x") + " " + point.get("y") + "\t");
//            }
//            System.out.println("\n");
        }
        return listAllLines;
    }
}

