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

        primaryStage.setFullScreen(true);

        // 3. OPTIONAL: Customize the Exit Hint
        // By default, JavaFX shows "Press ESC to exit full-screen"
        primaryStage.setFullScreenExitHint("Press ESC to window the game");

        // Use the manager to load the first screen
        SceneManager.switchScene("MainMenu.fxml");

        primaryStage.setTitle("DoorDasH");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}