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

import static jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType.D;

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
        graphicsContext.drawImage(image, 0, 0, width, height);

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
            bufferedImageGray = SwingFXUtils.fromFXImage(image, null);
            bufferedImageGray = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(bufferedImageGray, null);
            imageGray = SwingFXUtils.toFXImage(bufferedImageGray, null);
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
        graphicsContext.strokeOval((getCenterX() - getNormWidth()/2),
                (getCenterY() - getNormHeight()/2),
                getNormWidth(),
                getNormHeight());
    }

    public List<Map> getPoints (double rays, double step, double angle) {
        List<Map> listMaps = new ArrayList<>();

        if (rays == 1)
            angle = 0;
        else
            angle *= 2; // Inscribed angle

        double xStart, yStart, xEnd, yEnd;
        double radAlpha = step * Math.PI / 180;
        double radBeta = angle * Math.PI / 180;
        xStart = (getCenterX() + Math.sin(radAlpha) * (getNormWidth() / 2));
        yStart = (getCenterY() + Math.cos(radAlpha) * (getNormHeight() / 2));

        for (int i = 0; i < rays; i++) {
            if (rays == 1)
                rays = 0;   // To avoid calculating Not a Number
            double radGamma = radAlpha + Math.PI - (radBeta / 2) + (i / (rays - 1) * radBeta);
            xEnd = (getCenterX() + Math.sin(radGamma) * (getNormWidth() / 2));
            yEnd = (getCenterY() + Math.cos(radGamma) * (getNormHeight() / 2));

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

        for (Map <String,Double> line : listMaps) {
            xStart = line.get("xStart");
            yStart = line.get("yStart");
            xEnd = line.get("xEnd");
            yEnd = line.get("yEnd");
            graphicsContext.strokeLine(xStart,yStart,xEnd,yEnd);
        }
    }

    public List<Map> pointsToInt (double rays, double step, double angle) {
        List<Map> listInts = new ArrayList<>();
        double xStart, yStart, xEnd, yEnd;
        double x = getCenterX();
        double y = getCenterY();
        int xStart_int = 0, yStart_int = 0, xEnd_int = 0, yEnd_int = 0;

        List<Map> listMaps = getPoints(rays, step, angle);

        for (Map<String, Double> line : listMaps) {
            xStart = line.get("xStart");
            yStart = line.get("yStart");
            xEnd = line.get("xEnd");
            yEnd = line.get("yEnd");

            if (xStart > x) {
                if (yStart > y) {
                    xStart_int = (int) Math.floor(xStart);
                    yStart_int = (int) Math.floor(yStart);
                } else if (yStart < y) {
                    xStart_int = (int) Math.floor(xStart);
                    yStart_int = (int) Math.ceil(yStart);
                }
            } else if (xStart < x) {
                if (yStart > y) {
                    xStart_int = (int) Math.ceil(xStart);
                    yStart_int = (int) Math.floor(yStart);
                } else if (yStart < y) {
                    xStart_int = (int) Math.ceil(xStart);
                    yStart_int = (int) Math.ceil(yStart);
                }
            }

            if (xEnd > x) {
                if (yStart > y) {
                    xEnd_int = (int) Math.floor(xEnd);
                    yEnd_int = (int) Math.floor(yEnd);
                } else if (yStart < y) {
                    xEnd_int = (int) Math.floor(xEnd);
                    yEnd_int = (int) Math.ceil(yEnd);
                }
            } else if (xEnd < x) {
                if (yEnd > y) {
                    xEnd_int = (int) Math.ceil(xEnd);
                    yEnd_int = (int) Math.floor(yEnd);
                } else if (yEnd_int < y) {
                    xEnd_int = (int) Math.ceil(xEnd);
                    yEnd_int = (int) Math.ceil(yEnd);
                }
            }

            Map<String, Integer> mapInts = new HashMap<String, Integer>();
            mapInts.put("yEnd", yEnd_int);
            mapInts.put("xEnd", xEnd_int);
            mapInts.put("yStart", yStart_int);
            mapInts.put("xStart", xStart_int);

            listInts.add(mapInts);
            }
        return listInts;
    }

    public List<List> bresenham(double rays, double step, double angle) {
        int xStart, yStart, xEnd, yEnd;
        List<Map> listMaps = pointsToInt(rays, step, angle);
        List<Map> listPoints = new ArrayList<>();
        List<List> listLines = new ArrayList<>();

        for (Map<String, Integer> line : listMaps) {
            xStart = line.get("xStart");
            yStart = line.get("yStart");
            xEnd = line.get("xEnd");
            yEnd = line.get("yEnd");

            int w = xStart-xEnd;
            int h = yStart-yEnd;

            int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0 ;
            if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
            if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
            if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
            int longest = Math.abs(w) ;
            int shortest = Math.abs(h) ;
            if (!(longest>shortest)) {
                longest = Math.abs(h) ;
                shortest = Math.abs(w) ;
                if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
                dx2 = 0 ;
            }
            int numerator = longest >> 1 ;
            for (int i=0;i<=longest;i++) {
                Map<String, Integer> mapPoints = new HashMap<String, Integer>();
                mapPoints.put("yEnd", yEnd);
                mapPoints.put("xEnd", xEnd);
                listPoints.add(mapPoints);
                numerator += shortest ;
                if (!(numerator<longest)) {
                    numerator -= longest ;
                    xEnd += dx1 ;
                    yEnd += dy1 ;
                }
                else {
                    xEnd += dx2 ;
                    yEnd += dy2 ;
                }
            }
            listLines.add(listPoints);
            listPoints.clear();
        }
        return listLines;
    }
}

