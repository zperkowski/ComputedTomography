package com.zperkowski;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;

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
    public void generate() {
        System.out.println("Rays: " + slider_rays.getValue() + " Angle: " + slider_angle.getValue() + " Rotation:" + slider_rotation.getValue());
    }

    @FXML
    public void menu_new() {
        slider_rays.setValue(1.0);
        slider_angle.setValue(1.0);
        slider_rotation.setValue(0.0);
    }

    @FXML
    public void menu_open() {

    }

    @FXML
    public void menu_save() {

    }

    @FXML
    public void menu_about() {

    }
}
