package com.zperkowski;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class Tomograph {

    private Image image;
    private double rays, angle, step;
    private BufferedImage grayImage;
    private BufferedImage sinogram;

    private TaskGenerateSinogram sinogramGenerator;

    public Tomograph(Image image, double rays, double angle, double step) {
        this.image = image;
        this.rays = rays;
        this.angle = angle;
        this.step = step;

        grayImage = SwingFXUtils.fromFXImage(image, null);
        grayImage = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(grayImage, null);
        System.out.println(grayImage);

        sinogram = new BufferedImage((int) (360 / step), (int) rays, BufferedImage.TYPE_BYTE_GRAY);
        System.out.println(sinogram);
    }

    public Image getGrayPicture() {
        return SwingFXUtils.toFXImage(grayImage, null);
    }

    public TaskGenerateSinogram getSinogramGenerator() {
        if (sinogramGenerator == null)
            sinogramGenerator = new TaskGenerateSinogram();
        return sinogramGenerator;
    }


    class TaskGenerateSinogram extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            Task task = new Task<Double>() {
                protected Double call() throws Exception {
                    for (double i = 0; i < 360; i += step) {
                        //TODO: Generate a sinogram
                        Thread.sleep(75);
                        updateProgress((i / 359), 1.0);
                        System.out.println(i + "\t" + (i / 359));
                    }
                    updateProgress(1.0, 1.0);
                    return 1.0;
                }
            };
            return task;
        }
    }

}
