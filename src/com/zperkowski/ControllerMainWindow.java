package com.zperkowski;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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
        try {
            slider_rays.setValue(Double.parseDouble(valueOf(textfield_rays.getText())));
        } catch (NumberFormatException emptyStringError) {
            slider_rays.setValue(1.0);
        }
    }

    @FXML
    public void textfield_angle_edited() {
        try {
            slider_angle.setValue(Double.parseDouble(valueOf(textfield_angle.getText())));
        } catch (NumberFormatException emptyStringError) {
            slider_angle.setValue(1.0);
        }
    }

    @FXML
    public void textfield_rotation_edited() {
        try {
            slider_rotation.setValue(Double.parseDouble(valueOf(textfield_rotation.getText())));
        } catch (NumberFormatException emptyStringError) {
            slider_rotation.setValue(0.0);
        }
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
