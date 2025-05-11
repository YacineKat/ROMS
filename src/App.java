import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.net.URL;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Load splash screen
            URL splashUrl = getClass().getResource("splashE.fxml");
            if (splashUrl == null) {
                System.err.println("Error: splashE.fxml not found!");
                return;
            }
            Parent splashRoot = FXMLLoader.load(splashUrl);
            Scene splashScene = new Scene(splashRoot);

            // Show splash screen
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.setScene(splashScene);
            splashStage.show();

            // Set up pause transition before showing main window
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> {
                try {
                    // Load main welcome screen
                    URL welcomeUrl = getClass().getResource("welcome.fxml");
                    URL cssUrl = getClass().getResource("menu-style.css");

                    if (welcomeUrl == null) {
                        System.err.println("Error: welcome.fxml not found!");
                        return;
                    }

                    Parent mainRoot = FXMLLoader.load(welcomeUrl);
                    Scene mainScene = new Scene(mainRoot);

                    // Add CSS if available
                    if (cssUrl != null) {
                        mainScene.getStylesheets().add(cssUrl.toExternalForm());
                    }

                    // Close splash screen
                    splashStage.close();

                    // Create and configure main stage
                    Stage mainStage = new Stage();
                    mainStage.setTitle("ROMS Restaurant");
                    mainStage.setScene(mainScene);

                    // Set initial window size and position
                    mainStage.setWidth(1000);
                    mainStage.setHeight(700);
                    mainStage.centerOnScreen();

                    // Initialize stage manager with main stage
                    StageManager.initialize(mainStage);

                    // Show main window
                    mainStage.show();
                } catch (Exception e) {
                    System.err.println("Error loading welcome.fxml or CSS: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            pause.play();
        } catch (Exception e) {
            System.err.println("Error in App.start(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}