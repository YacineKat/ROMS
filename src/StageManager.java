package com.restaurant.roms;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class StageManager {
    private static Stage primaryStage;
    private static boolean isMaximized;
    private static double width;
    private static double height;

    public static void initialize(Stage stage) {
        primaryStage = stage;
        isMaximized = stage.isMaximized();
        width = stage.getWidth();
        height = stage.getHeight();

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            isMaximized = newVal;
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) {
                width = newVal.doubleValue();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) {
                height = newVal.doubleValue();
            }
        });
    }

    public static void initializeStage(Stage stage) {
        if (stage == null) return;
        
        try {
            // Initialize stage with basic settings before showing
            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(true);
            
            // Set minimum size
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Set default size
            stage.setWidth(1100);
            stage.setHeight(700);
            
            // Center on screen
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error initializing stage: " + e.getMessage());
        }
    }
    
    public static void applyStageSettings(Stage stage) {
        if (stage == null) return;
        
        try {
            // Only apply settings if the stage is not showing
            if (!stage.isShowing()) {
                stage.initStyle(StageStyle.DECORATED);
                stage.setResizable(true);
                stage.centerOnScreen();
                
                // Set minimum size
                stage.setMinWidth(800);
                stage.setMinHeight(600);
                
                // Set default size
                stage.setWidth(1100);
                stage.setHeight(700);
            }
        } catch (Exception e) {
            System.err.println("Error applying stage settings: " + e.getMessage());
        }
    }
}