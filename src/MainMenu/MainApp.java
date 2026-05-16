package MainMenu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    // Inside MainApp.java

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize the stage reference once at startup
        SceneManager.setStage(primaryStage);

        try {
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/MainMenu/assets/images/my_logo.png")));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            System.out.println("Could not load app logo. Check the file path!");
        }

        // Set an initial empty scene so the first switchScene doesn't fail
        primaryStage.setScene(new Scene(new javafx.scene.layout.Pane(), 1200, 800));

        // Navigate to Main Menu
        SceneManager.switchScene("MainMenu.fxml");

        primaryStage.setResizable(true);
        primaryStage.setTitle("DoorDasH");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}