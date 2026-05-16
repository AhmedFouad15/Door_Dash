package MainMenu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import game.engine.Role;

public class GameSetupWindow {

    public static void display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Game Setup");
        window.setMinWidth(500);
        window.setMinHeight(600);

        // --- 1. INSTRUCTIONS SCREEN ---
        VBox instructionsLayout = new VBox(20);
        instructionsLayout.setPadding(new Insets(20));
        instructionsLayout.setAlignment(Pos.CENTER);
        instructionsLayout.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Welcome to Monstropolis");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");

        Text rulesText = new Text(
                "Welcome to Monstropolis, where every move is a race for energy and survival.\n\n" +
                        "First: Select your role:\n" +
                        "  * SCARER → Gain energy through screams\n" +
                        "  * LAUGHER → Gain energy through laughter\n" +
                        "* After choosing, you will be assigned a monster automatically.\n\n" +
                        "You and your opponent will both start at cell 0.\n" +
                        "Each monster starts with its own initial energy.\n\n" +
                        "How to Play:\n" +
                        "1. Power-Up (Optional)\n" +
                        "  * Activate your monster’s special ability (costs energy).\n" +
                        "2. Roll the Dice\n" +
                        "  * Move forward based on the number rolled.\n" +
                        "3. Land on a Cell (Different cells have different effects):\n" +
                        "  * Doors → Gain or lose energy depending on your role\n" +
                        "  * Monster Cells → Trigger special interactions (color: blue)\n" +
                        "  * Card Cells → Draw a card with a special effect (color: red)\n" +
                        "  * Conveyor Belts → Move forward (color: green)\n" +
                        "  * Contamination Socks → Move backward and lose energy (color: orange)\n" +
                        "  * Normal Cells → No effect (color: yellow)\n\n" +
                        "Important Rules:\n" +
                        "1. You cannot land on a cell occupied by the opponent.\n" +
                        "2. Some effects impact both players.\n" +
                        "3. Shields can block negative energy effects.\n\n" +
                        "Winning Condition:\n" +
                        "1. Reach the final cell AND have at least 1000 energy."
        );
        rulesText.setStyle("-fx-fill: white; -fx-font-size: 14px;");
        rulesText.setWrappingWidth(450);

        // Make the text scrollable just in case the screen is small
        ScrollPane scrollPane = new ScrollPane(rulesText);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-border-color: transparent;");
        scrollPane.setFitToWidth(true);

        Button continueBtn = new Button("Continue");
        continueBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        instructionsLayout.getChildren().addAll(title, scrollPane, continueBtn);
        Scene instructionsScene = new Scene(instructionsLayout, 500, 600);

        // --- 2. ROLE SELECTION SCREEN ---
        VBox roleLayout = new VBox(30);
        roleLayout.setPadding(new Insets(20));
        roleLayout.setAlignment(Pos.CENTER);
        roleLayout.setStyle("-fx-background-color: #2b2b2b;");

        Label roleTitle = new Label("CHOOSE YOUR DESTINY");
        roleTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button btnScarer = new Button("SCARER");
        btnScarer.setStyle("-fx-background-color: #8a2be2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 30;");

        Button btnLaugher = new Button("LAUGHER");
        btnLaugher.setStyle("-fx-background-color: #ffd700; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 15 30;");

        HBox buttonsBox = new HBox(40, btnScarer, btnLaugher);
        buttonsBox.setAlignment(Pos.CENTER);

        roleLayout.getChildren().addAll(roleTitle, buttonsBox);
        Scene roleScene = new Scene(roleLayout, 500, 600);

        // --- 3. ACTIONS ---
        continueBtn.setOnAction(e -> window.setScene(roleScene));

        btnScarer.setOnAction(e -> {
            GameController.playerRole = Role.SCARER; // Set the static variable
            window.close();
            SceneManager.switchScene("GameScreen.fxml"); // Safely launch game
        });

        btnLaugher.setOnAction(e -> {
            GameController.playerRole = Role.LAUGHER; // Set the static variable
            window.close();
            SceneManager.switchScene("GameScreen.fxml"); // Safely launch game
        });

        // Start by showing the instructions first
        window.setScene(instructionsScene);
        window.showAndWait();
    }
}