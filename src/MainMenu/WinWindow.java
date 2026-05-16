package MainMenu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import game.engine.monsters.Monster;
import game.engine.Role;

import java.util.Objects;

public class WinWindow {

    public static void display(Monster winner, Stage primaryStage) {
        Stage window = new Stage();
        // Blocks interaction with the game board until they click a button
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Game Over!");
        window.setMinWidth(400);

        // 1. Play the Victory Sound using your existing assets
        try {
            // Assuming your SoundManager has a playSound method. If not, you can use AudioClip directly:
            String soundPath = Objects.requireNonNull(WinWindow.class.getResource("/MainMenu/assets/audio/victory_fanfare.wav")).toExternalForm();
            javafx.scene.media.AudioClip winSound = new javafx.scene.media.AudioClip(soundPath);
            winSound.play();
        } catch (Exception e) {
            System.out.println("Could not load victory sound.");
        }

        // 2. Winner Text
        Label congratsLabel = new Label("🎉 CONGRATULATIONS! 🎉");
        congratsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFD700;"); // Gold color

        Label winnerNameLabel = new Label(winner.getName() + " (" + winner.getRole() + ") takes the win!");
        winnerNameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        // 3. Winner Image (Using your tokens based on Role)
        ImageView winnerImage = new ImageView();
        try {
            String imagePath = winner.getRole() == Role.SCARER ?
                    "/MainMenu/assets/images/scarer_token.png" :
                    "/MainMenu/assets/images/laugher_token.png";

            Image img = new Image(Objects.requireNonNull(WinWindow.class.getResourceAsStream(imagePath)));
            winnerImage.setImage(img);
            winnerImage.setFitHeight(150);
            winnerImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Could not load winner image.");
        }

        // 4. Buttons
        Button playAgainBtn = new Button("Play Again");
        Button menuBtn = new Button("Main Menu");

        // Style buttons to match your menu.css vibe
        String btnStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;";
        playAgainBtn.setStyle(btnStyle);
        menuBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        // Button Actions using your SceneManager
        // Inside WinWindow.java, fix your button actions like this:

        playAgainBtn.setOnAction(e -> {
            window.close();
            // CORRECTED: Call the static method directly
            SceneManager.switchScene("GameScreen.fxml");
        });

        menuBtn.setOnAction(e -> {
            window.close();
            // CORRECTED: Call the static method directly
            SceneManager.switchScene("MainMenu.fxml");
        });

        HBox buttonLayout = new HBox(20);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(playAgainBtn, menuBtn);

        // 5. Layout Setup
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2b2b2b;"); // Dark background to make image/text pop
        layout.getChildren().addAll(congratsLabel, winnerImage, winnerNameLabel, buttonLayout);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}