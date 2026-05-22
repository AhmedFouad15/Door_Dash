package MainMenu;

import game.engine.monsters.Monster;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Objects;

import static MainMenu.SoundManager.playLose_game;

public class WinWindow {

    public static void display(Monster winner, Monster player, Monster opponent, Stage primaryStage) {
        display(winner, player, opponent, primaryStage, "You", "Opponent", false);
    }

    public static void display(Monster winner, Monster player, Monster opponent, Stage primaryStage,
                               String playerDisplayName, String opponentDisplayName, boolean singlePlayerMode) {
        Stage window = new Stage();
        window.initOwner(primaryStage);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Game Over!");
        window.setMinWidth(460);
        window.setResizable(false);

        Image logo = loadImage("/MainMenu/assets/images/my_logo.png");
        if (logo != null) {
            window.getIcons().add(logo);
        }

        boolean playerWon = winner == player;
        String winnerDisplayName = playerWon ? playerDisplayName : opponentDisplayName;

        ImageView logoView = new ImageView(logo);
        logoView.setFitHeight(72);
        logoView.setPreserveRatio(true);

        Label congratsLabel = new Label(singlePlayerMode ? (playerWon ? "YOU WON!" : "YOU LOST!") : "GAME OVER");
        if(playerWon == false && singlePlayerMode == true){
            String soundPath = Objects.requireNonNull(WinWindow.class.getResource("/MainMenu/assets/audio/game_lost.mp3")).toExternalForm();
            javafx.scene.media.AudioClip winSound = new javafx.scene.media.AudioClip(soundPath);
            winSound.play();
        }
        else{
            try {
                String soundPath = Objects.requireNonNull(WinWindow.class.getResource("/MainMenu/assets/audio/victory_fanfare.wav")).toExternalForm();
                javafx.scene.media.AudioClip winSound = new javafx.scene.media.AudioClip(soundPath);
                winSound.play();
            } catch (Exception e) {
                System.out.println("Could not load victory sound.");
            }
        }
        congratsLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
        congratsLabel.setTextAlignment(TextAlignment.CENTER);

        Label winnerNameLabel = new Label(winnerDisplayName + " wins with " + winner.getName());
        winnerNameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        winnerNameLabel.setWrapText(true);
        winnerNameLabel.setMaxWidth(380);
        winnerNameLabel.setAlignment(Pos.CENTER);
        winnerNameLabel.setTextAlignment(TextAlignment.CENTER);

        Label winnerRoleLabel = new Label("Role: " + winner.getRole() + "   Energy: " + winner.getEnergy());
        winnerRoleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00d4ff; -fx-font-weight: bold;");
        winnerRoleLabel.setAlignment(Pos.CENTER);
        winnerRoleLabel.setTextAlignment(TextAlignment.CENTER);

        Label finalEnergyLabel = new Label(
                "Final Energy\n" +
                        playerDisplayName + " - " + player.getName() + ": " + player.getEnergy() + "\n" +
                        opponentDisplayName + " - " + opponent.getName() + ": " + opponent.getEnergy()
        );
        finalEnergyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-line-spacing: 4px;");
        finalEnergyLabel.setWrapText(true);
        finalEnergyLabel.setMaxWidth(380);
        finalEnergyLabel.setAlignment(Pos.CENTER);
        finalEnergyLabel.setTextAlignment(TextAlignment.CENTER);

        ImageView winnerImage = new ImageView(loadWinnerImage(winner));
        winnerImage.setFitHeight(150);
        winnerImage.setFitWidth(190);
        winnerImage.setPreserveRatio(true);

        Button playAgainBtn = new Button("Play Again");
        Button menuBtn = new Button("Main Menu");

        playAgainBtn.setStyle("-fx-background-color: #00d4ff; -fx-text-fill: #061018; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 22; -fx-background-radius: 6;");
        menuBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 22; -fx-background-radius: 6;");

        playAgainBtn.setOnAction(e -> {
            window.close();
            GameSetupWindow.display();
        });

        menuBtn.setOnAction(e -> {
            window.close();
            SceneManager.switchScene("MainMenu.fxml");
        });

        HBox buttonLayout = new HBox(18, playAgainBtn, menuBtn);
        buttonLayout.setAlignment(Pos.CENTER);

        VBox layout = new VBox(14);
        layout.setPadding(new Insets(26));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(
                "-fx-background-color: rgba(8, 18, 28, 0.98); " +
                        "-fx-border-color: #00d4ff; " +
                        "-fx-border-width: 2;"
        );
        layout.getChildren().addAll(logoView, congratsLabel, winnerImage, winnerNameLabel, winnerRoleLabel, finalEnergyLabel, buttonLayout);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

    private static Image loadWinnerImage(Monster winner) {
        Image monsterImage = loadImage(getMonsterImagePath(winner));
        if (monsterImage != null) return monsterImage;
        return loadImage("/MainMenu/assets/images/my_logo.png");
    }

    private static String getMonsterImagePath(Monster winner) {
        if (winner == null || winner.getName() == null) return "/MainMenu/assets/images/my_logo.png";

        switch (winner.getName()) {
            case "James P. Sullivan": return "/MainMenu/assets/images/scarer_token.png";
            case "Mike Wazowski": return "/MainMenu/assets/images/laugher_token.png";
            case "Randall Boggs": return "/MainMenu/assets/images/randall.png";
            case "Celia Mae": return "/MainMenu/assets/images/celia.png";
            case "Henry J. Waternoose": return "/MainMenu/assets/images/Henry.png";
            case "Roz": return "/MainMenu/assets/images/roz.png";
            case "Fungus": return "/MainMenu/assets/images/fungus.png";
            case "Yeti": return "/MainMenu/assets/images/Yeti.png";
            default: return "/MainMenu/assets/images/my_logo.png";
        }
    }

    private static Image loadImage(String path) {
        try {
            InputStream stream = WinWindow.class.getResourceAsStream(path);
            return stream == null ? null : new Image(stream);
        } catch (Exception e) {
            return null;
        }
    }
}
