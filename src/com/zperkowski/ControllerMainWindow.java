package com.zperkowski;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;


public class ControllerMainWindow {

    @FXML
    Slider slider_rays;
    @FXML
    Slider slider_angle;
    @FXML
    Slider slider_step;
    @FXML
    Canvas canvas_center;
    @FXML
    Canvas canvas_right;
    @FXML
    TextField textfield_rays;
    @FXML
    TextField textfield_angle;
    @FXML
    TextField textfield_step;
    @FXML
    Button button_generate;
    @FXML
    ProgressBar progressBar;

    private ProcessingImage pImage;
    private GraphicsContext gc_left;
    private GraphicsContext gc_right;

    @FXML
    public void initialize() {
        gc_left = canvas_center.getGraphicsContext2D();
        gc_right = canvas_right.getGraphicsContext2D();
        menu_new();
    }

    @FXML
    public void generate() {
        System.out.println("Rays: " + slider_rays.getValue() + " Angle: " + slider_angle.getValue() + " Step:" + slider_step.getValue());
        slider_rays_edited();
        slider_angle_edited();
        slider_step_edited();
        Tomograph tomograph = new Tomograph(pImage, slider_rays.getValue(),
                                        slider_angle.getValue(),
                                        slider_step.getValue(),
                                        gc_left,
                                        gc_right);
        pImage.getGraphicsContext().drawImage(pImage.getImageGray(), 0, 0);
        progressBar.progressProperty().bind(tomograph.getSinogramGenerator().progressProperty());
        tomograph.getSinogramGenerator().start();
        pImage.drawOval();
    }

    @FXML
    public void menu_new() {
        slider_rays.setValue(1.0);
        slider_angle.setValue(1.0);
        slider_step.setValue(1.0);
        slider_angle_edited();
        slider_rays_edited();
        slider_step_edited();
        progressBar.setProgress(0.0);
        button_generate.setDisable(true);
        if (pImage != null) {
            pImage.clearCanvas();
            pImage = null;
        }
        if (gc_right != null)
            gc_right.clearRect(0, 0, gc_right.getCanvas().getWidth(), gc_right.getCanvas().getHeight());
    }

    @FXML
    public void menu_open() {
        Image image = openPictureDialog();
        if (image != null) {
            pImage = new ProcessingImage(image, canvas_center);
            button_generate.setDisable(false);
        }
    }

    @FXML
    public void menu_save() {

    }

    @FXML
    public void menu_about() {

    }

    @FXML
    public void slider_rays_edited() {
        textfield_rays.setText(String.valueOf(slider_rays.getValue()));
    }

    @FXML
    public void slider_angle_edited() {
        textfield_angle.setText(String.valueOf(slider_angle.getValue()));
    }

    @FXML
    public void slider_step_edited() {
        textfield_step.setText(String.valueOf(slider_step.getValue()));
    }

    @FXML
    public void textfield_rays_edited() {
        setValue(textfield_rays, slider_rays);
    }

    @FXML
    public void textfield_angle_edited() {
        setValue(textfield_angle, slider_angle);
    }

    @FXML
    public void textfield_step_edited() {
        setValue(textfield_step, slider_step);
    }

    private void setValue(TextField textField, Slider slider) {
        try {
            double oldValue = Double.parseDouble(textField.getText());
            double newValue = getValue(oldValue, textField, slider);
            slider.setValue(newValue);
            // Compares two values to avoid loosing cursor position on the TextField.
            // Method setText() moves the position to the begin.
            if (oldValue != newValue)
                textField.setText(String.valueOf(newValue));
        } catch (NumberFormatException emptyStringError) {
            slider.setValue(slider.getMin());
        }
    }

    /* Check if the value is between minimum and maximum possible value in a slider.
        If value is to high returns maximum value of slider.
        If value is to low returns minimum value of slider. */
    private double getValue(double value, TextField textField, Slider slider) {
        if (value < 0) {
            value = 0.0;
            textField.setText(String.valueOf(value));
        } else if (value > slider.getMax()) {
            value = slider.getMax();
            textField.setText(String.valueOf(value));
        }
        return value;
    }

    private Image openPictureDialog() {
        Image image;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a picture");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("GIF", "*.gif")
        );
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            image = new Image(file.toURI().toString());
            return image;
        }
        return null;
    }

}
