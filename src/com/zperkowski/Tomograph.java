package com.zperkowski;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.awt.image.BufferedImage;

public class Tomograph {

    private ProcessingImage pImage;
    private double rays, angle, step;
    private BufferedImage sinogram;

    private TaskGenerateSinogram sinogramGenerator;

    public Tomograph(ProcessingImage pImage, double rays, double angle, double step) {
        this.pImage = pImage;
        this.rays = rays;
        this.angle = angle;
        this.step = step;

        sinogram = new BufferedImage((int) (360 / step), (int) rays, BufferedImage.TYPE_BYTE_GRAY);
        System.out.println(sinogram);
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
