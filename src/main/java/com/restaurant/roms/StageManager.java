package com.restaurant.roms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class StageManager {
    private static StageManager instance;
    private static Stage primaryStage;
    private static boolean isMaximized;
    private static double width;
    private static double height;

    private StageManager() {}

    public static StageManager getInstance() {
        if (instance == null) {
            instance = new StageManager();
        }
        return instance;
    }

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

    public static void applyStageSettings(Stage stage) {
        stage.setResizable(true);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.centerOnScreen();
    }

    public static void loadScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }

    public static void loadScene(String fxmlPath, Object controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
        loader.setController(controller);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
}