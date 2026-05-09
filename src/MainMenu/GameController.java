package MainMenu;

import game.engine.Game;
import game.engine.Role;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Monster;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Random;

public class GameController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private Pane particlePane;

    // UI Elements
    @FXML private GridPane boardGrid;
    @FXML private Label lblTurnInfo;
    @FXML private VBox playerStatsBox;
    @FXML private Label lblPlayerStats;
    @FXML private Label lblOpponentStats;
    @FXML private TextArea actionLog;

    @FXML private Button btnRollDice;
    @FXML private Button btnPowerup;

    // Backend Connection
    private Game gameEngine;

    // Keep track of our GUI cells (Index 0 to 99)
    private StackPane[] guiCells = new StackPane[100];

    @FXML
    public void initialize() {
        // 1. Setup Cyber-Premium Visuals
        setupVisuals();

        // 2. Initialize the Backend Engine
        try {
            // For now, we default to SCARER. You can add a role selection screen later!
            gameEngine = new Game(Role.SCARER);
            log("Game initialized. You are playing as a " + Role.SCARER);
        } catch (IOException e) {
            log("CRITICAL ERROR: Failed to load CSV data! " + e.getMessage());
            return;
        }

        // 3. Build the GUI Board (100 cells, Zigzag)
        generateBoard();

        // 4. Setup Button Actions
        setupActions();

        // 5. Initial Sync
        updateUI();
    }

    private void generateBoard() {
        boardGrid.getChildren().clear();

        for (int i = 0; i < 100; i++) {
            StackPane cell = new StackPane();

            // Glassmorphism Cell Style
            cell.setPrefSize(60, 60);
            cell.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); " +
                    "-fx-border-color: rgba(0, 212, 255, 0.2); " +
                    "-fx-border-width: 1; -fx-border-radius: 5;");

            // Add cell number for visual tracking
            Label cellNumber = new Label(String.valueOf(i));
            cellNumber.setStyle("-fx-text-fill: rgba(255,255,255,0.2);");
            cell.getChildren().add(cellNumber);

            guiCells[i] = cell;

            // --- ZIGZAG MATHEMATICS ---
            // Row 9 is the bottom, Row 0 is the top.
            int row = 9 - (i / 10);

            // Even rows (from bottom) go Left-to-Right. Odd rows go Right-to-Left.
            int col = (i / 10) % 2 == 0 ? (i % 10) : (9 - (i % 10));

            boardGrid.add(cell, col, row);
        }
    }

    private void setupActions() {
        btnRollDice.setOnAction(e -> {
            try {
                // Call the Engine!
                gameEngine.playTurn();
                log(gameEngine.getCurrent().getName() + " rolled the dice and ended their turn.");

            } catch (InvalidMoveException ex) {
                // Milestone 3: Handle Exceptions Gracefully
                showError("Invalid Move", ex.getMessage());
                log("Move failed: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace(); // Catch any unexpected engine errors
            }

            updateUI();
            checkWinCondition();
        });

        btnPowerup.setOnAction(e -> {
            try {
                // Call the Engine!
                gameEngine.usePowerup();
                log(gameEngine.getCurrent().getName() + " used their powerup!");
            } catch (OutOfEnergyException ex) {
                // Milestone 3: Handle Exceptions Gracefully
                showError("Not Enough Energy", ex.getMessage());
                log("Powerup failed: " + ex.getMessage());
            }
            updateUI();
        });
    }

    private void updateUI() {
        // 1. Update Turn Info
        lblTurnInfo.setText("TURN: " + gameEngine.getCurrent().getName().toUpperCase());

        // 2. Update Stats Panel
        Monster player = gameEngine.getPlayer();
        Monster opponent = gameEngine.getOpponent();

        lblPlayerStats.setText(
                player.getName() + " (YOU)\n" +
                        "Energy: " + player.getEnergy() + "\n" +
                        "Position: " + player.getPosition()
        );

        lblOpponentStats.setText(
                opponent.getName() + " (OPPONENT)\n" +
                        "Energy: " + opponent.getEnergy() + "\n" +
                        "Position: " + opponent.getPosition()
        );

        // 3. Clear all monsters from the board GUI
        for (StackPane cell : guiCells) {
            // Keep the cell number, remove anything else (like monsters)
            cell.getChildren().removeIf(node -> node instanceof Circle);
        }

        // 4. Draw Opponent
        drawMonsterOnGrid(opponent.getPosition(), Color.web("#ff003c")); // Red for opponent

        // 5. Draw Player (Drawn second so it stays on top if they share a cell)
        drawMonsterOnGrid(player.getPosition(), Color.web("#00d4ff")); // Neon Blue for player
    }

    private void drawMonsterOnGrid(int position, Color color) {
        if (position >= 0 && position < 100) {
            Circle monsterToken = new Circle(15, color);
            monsterToken.setEffect(new GaussianBlur(5)); // Glow effect
            guiCells[position].getChildren().add(monsterToken);
        }
    }

    private void checkWinCondition() {
        Monster winner = gameEngine.getWinner();
        if (winner != null) {
            log("GAME OVER! " + winner.getName() + " WINS!");
            showError("VICTORY!", winner.getName() + " has reached Boo's Door with enough energy!");

            // Disable controls
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);
        }
    }

    private void log(String message) {
        actionLog.appendText(message + "\n");
    }

    private void showError(String title, String message) {
        // According to Milestone 3: Show visual indicator for invalid actions without crashing
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        // Add custom styling here if you want to make the alert match your theme
        alert.showAndWait();
    }

    // --- VISUALS & PARTICLES (From MainMenu) ---
    private void setupVisuals() {
        bgImage.setPreserveRatio(false);
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        Platform.runLater(this::createParticles);
    }

    private void createParticles() {
        particlePane.setPrefSize(rootPane.getWidth(), rootPane.getHeight());
        Random rand = new Random();
        double width = rootPane.getWidth();
        double height = rootPane.getHeight();

        if (width <= 0) width = 1000;
        if (height <= 0) height = 800;

        for (int i = 0; i < 40; i++) {
            Circle particle = new Circle(rand.nextDouble() * 5 + 2);
            particle.setFill(Color.web("#00d4ff", 0.5));
            particle.setEffect(new GaussianBlur(rand.nextDouble() * 5 + 5));

            particle.setTranslateX(rand.nextDouble() * width);
            particle.setTranslateY(height + (rand.nextDouble() * 200));
            particlePane.getChildren().add(particle);

            TranslateTransition floatUp = new TranslateTransition(Duration.seconds(rand.nextDouble() * 10 + 10), particle);
            floatUp.setByY(-(height + 400));
            floatUp.setCycleCount(Animation.INDEFINITE);

            FadeTransition pulse = new FadeTransition(Duration.seconds(rand.nextDouble() * 2 + 2), particle);
            pulse.setFromValue(0.2);
            pulse.setToValue(0.8);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);

            floatUp.play();
            pulse.play();
        }
    }
}