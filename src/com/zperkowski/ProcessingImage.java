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

    public void drawRays(int quantity, double degree) {
        //TODO: Should draw as many as given parameter
        graphicsContext.setStroke(Color.RED);
        int xStart, yStart, xEnd, yEnd;
        double rad = degree * Math.PI / 180;
        xStart = getCenterX();
        yStart = getCenterY();
        xEnd = (int) (xStart + Math.sin(rad) * (getNormWidth() / 2));
        yEnd = (int) (yStart + Math.cos(rad) * (getNormWidth() / 2));
        graphicsContext.strokeLine(xStart, yStart, xEnd, yEnd);
    }
}
