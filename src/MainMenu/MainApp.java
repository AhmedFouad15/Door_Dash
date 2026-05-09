package MainMenu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.setStage(primaryStage);
        SceneManager.switchScene("MainMenu.fxml");

        // Ensure it can still be resized after they press ESC
        primaryStage.setResizable(true);

        primaryStage.setTitle("DoorDasH");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}