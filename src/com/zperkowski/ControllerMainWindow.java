package com.zperkowski;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

import static java.lang.String.valueOf;

public class ControllerMainWindow {

    @FXML
    Slider slider_rays;
    @FXML
    Slider slider_angle;
    @FXML
    Slider slider_rotation;
    @FXML
    Canvas canvas_center;
    @FXML
    Canvas canvas_right;
    @FXML
    TextField textfield_rays;
    @FXML
    TextField textfield_angle;
    @FXML
    TextField textfield_rotation;

    @FXML
    public void initialize() {
        menu_new();
    }

    @FXML
    public void generate() {
        System.out.println("Rays: " + slider_rays.getValue() + " Angle: " + slider_angle.getValue() + " Rotation:" + slider_rotation.getValue());
        slider_rays_edited();
        slider_angle_edited();
        slider_rotation_edited();
        GraphicsContext gc = canvas_right.getGraphicsContext2D();
        draw_oval( gc );
    }

    private void draw_oval(GraphicsContext gc) {
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        gc.setStroke(Color.RED);
        gc.strokeOval(10, 10, canvasWidth-20, canvasHeight-20);
    }

    @FXML
    public void menu_new() {
        slider_rays.setValue(1.0);
        slider_angle.setValue(1.0);
        slider_rotation.setValue(0.0);
        slider_angle_edited();
        slider_rays_edited();
        slider_rotation_edited();
    }

    @FXML
    public void menu_open() {
        Image image = openPictureDialog();
        GraphicsContext gc = canvas_right.getGraphicsContext2D();
        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();
        gc.drawImage(image, 0, 0, canvasWidth, canvasHeight);
    }

    @FXML
    public void menu_save() {

    }

    @FXML
    public void menu_about() {

    }

    @FXML
    public void slider_rays_edited() {
        textfield_rays.setText(valueOf(slider_rays.getValue()));
    }

    @FXML
    public void slider_angle_edited() {
        textfield_angle.setText(valueOf(slider_angle.getValue()));
    }

    @FXML
    public void slider_rotation_edited() {
        textfield_rotation.setText(valueOf(slider_rotation.getValue()));
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
    public void textfield_rotation_edited() {
        setValue(textfield_rotation, slider_rotation);
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
