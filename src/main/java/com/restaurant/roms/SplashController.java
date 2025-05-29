package com.restaurant.roms;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class SplashController {
    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        // Load the logo image
        try {
            logoImage.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
        }
    }
}
