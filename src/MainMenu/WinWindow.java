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

    public static void display(Monster winner, Monster player, Monster opponent, Stage primaryStage) {
        display(winner, player, opponent, primaryStage, "You", "Opponent", false);
    }

    public static void display(Monster winner, Monster player, Monster opponent, Stage primaryStage,
                               String playerDisplayName, String opponentDisplayName, boolean singlePlayerMode) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Game Over!");
        window.setMinWidth(400);

        try {
            String soundPath = Objects.requireNonNull(WinWindow.class.getResource("/MainMenu/assets/audio/victory_fanfare.wav")).toExternalForm();
            javafx.scene.media.AudioClip winSound = new javafx.scene.media.AudioClip(soundPath);
            winSound.play();
        } catch (Exception e) {
            System.out.println("Could not load victory sound.");
        }

        boolean playerWon = winner == player;
        String winnerDisplayName = playerWon ? playerDisplayName : opponentDisplayName;

        Label congratsLabel = new Label(singlePlayerMode ? (playerWon ? "YOU WON!" : "YOU LOST!") : "CONGRATULATIONS!");
        congratsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");

        Label winnerNameLabel = new Label(winnerDisplayName + " - " + winner.getName() + " (" + winner.getRole() + ") takes the win!");
        winnerNameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        Label finalEnergyLabel = new Label(
                "Final Energy\n" +
                        playerDisplayName + " - " + player.getName() + " (" + player.getRole() + "): " + player.getEnergy() + "\n" +
                        opponentDisplayName + " - " + opponent.getName() + " (" + opponent.getRole() + "): " + opponent.getEnergy()
        );
        finalEnergyLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: white; -fx-alignment: center;");
        finalEnergyLabel.setWrapText(true);

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

        Button playAgainBtn = new Button("Play Again");
        Button menuBtn = new Button("Main Menu");

        playAgainBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        menuBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        playAgainBtn.setOnAction(e -> {
            window.close();
            GameSetupWindow.display();
        });

        menuBtn.setOnAction(e -> {
            window.close();
            SceneManager.switchScene("MainMenu.fxml");
        });

        HBox buttonLayout = new HBox(20);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().addAll(playAgainBtn, menuBtn);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2b2b2b;");
        layout.getChildren().addAll(congratsLabel, winnerImage, winnerNameLabel, finalEnergyLabel, buttonLayout);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }
}