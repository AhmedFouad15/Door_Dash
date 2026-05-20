package MainMenu;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static Stage getStage() {
        return stage;
    }

    public static void switchScene(String fxmlFile) {
        // Triggers the fade out before loading the new scene
        if (stage.getScene() != null && stage.getScene().getRoot() != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> loadNewScene(fxmlFile));
            fadeOut.play();
        } else {
            loadNewScene(fxmlFile);
        }
    }

    private static void loadNewScene(String fxmlFile) {
        try {
            String path = "/MainMenu/" + fxmlFile;
            URL resource = SceneManager.class.getResource(path);

            if (resource == null) {
                System.out.println("FAILED TO FIND: " + path);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (root instanceof javafx.scene.layout.Region) {
                String imageUrl = SceneManager.class
                        .getResource("/MainMenu/assets/images/bg.jpeg")
                        .toExternalForm();

                ((javafx.scene.layout.Region) root).setStyle(
                        "-fx-background-image: url('" + imageUrl + "');" +
                                "-fx-background-size: cover;" +
                                "-fx-background-position: center;"
                );
            }

            // --- THE FIX STARTS HERE ---
            // Get current dimensions BEFORE switching the scene
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            root.setOpacity(0.0);

            // Create the scene WITHOUT hardcoded numbers
            Scene scene = new Scene(root);

            // Apply your CSS
            URL cssResource = SceneManager.class.getResource("menu.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setScene(scene);

            // Re-apply the previous dimensions so the window doesn't "snap"
            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            // --- THE FIX ENDS HERE ---

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}