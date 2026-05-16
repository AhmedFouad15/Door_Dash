package MainMenu;

import game.engine.Constants;
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
import javafx.scene.image.Image;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class GameController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private Pane particlePane;

    @FXML private GridPane boardGrid;
    @FXML private Label lblTurnInfo;
    @FXML private VBox playerStatsBox;
    @FXML private Label lblPlayerStats;
    @FXML private Label lblOpponentStats;
    @FXML private TextArea actionLog;

    @FXML private Button btnRollDice;
    @FXML private Button btnPowerup;
    @FXML private Button HomeMenu;


    // The Engines
    private Game gameEngine;
    private BoardRenderer boardRenderer;

    public static Role playerRole = Role.SCARER;

    public void initialize() {
        SoundManager.init();
        setupVisuals();

        try {
            // Modify this line to use the static variable chosen in the popup!
            gameEngine = new Game(playerRole);
            log("SYSTEM: Game Engine Initialized as " + playerRole);
        } catch (Exception e) {
            log("CRITICAL ERROR: Could not load Game Backend.");
            e.printStackTrace();
            return;
        }

        boardRenderer = new BoardRenderer(boardGrid);
        boardRenderer.renderInitialBoard(gameEngine.getBoard());

        setupActions();
        updateUI();
    }

    private void setupActions() {
        btnRollDice.setOnAction(e -> {
            // 1. Initial Checks & Disable button so player can't double-click
            if (gameEngine == null) return;
            btnRollDice.setDisable(true);

            // Priority 5: Play sound when dice are rolled
            SoundManager.playDice();

            // --- 1. RECORD STATE BEFORE THE MOVE ---
            Monster activeMonster = gameEngine.getCurrent();
            Monster opponent = gameEngine.getOpponent();
            int prePosPlayer = activeMonster.getPosition();
            int preEnergyPlayer = activeMonster.getEnergy();
            int preEnergyOpponent = opponent.getEnergy();
            boolean preShieldPlayer = activeMonster.isShielded();
            game.engine.Role preRolePlayer = activeMonster.getRole();

            // --- 2. ROLL THE DICE ---
            // --- 2. ROLL THE DICE ---
            AnimationManager.animateDiceRoll(btnRollDice, () -> {
                try {
                    gameEngine.playTurn();
                    SoundManager.playMove();

                    int postPosPlayer = activeMonster.getPosition();
                    int spacesMoved = postPosPlayer - prePosPlayer;
                    if (spacesMoved > 0 && spacesMoved <= 6) {
                        log(activeMonster.getName() + " rolled a " + spacesMoved + ".");
                    }

                    // WAIT FOR THE VISUAL WALK TO FINISH BEFORE SHOWING POPUPS
                    updateUI(() -> {
                        // --- 3. COMPARE STATE FOR SOUNDS & VISUALS ---
                        StackPane playerCell = boardRenderer.getCellVisual(activeMonster.getPosition());

                        // EVENT: Damage/Energy Loss
                        if (activeMonster.getEnergy() < preEnergyPlayer) {
                            SoundManager.playDamage();
                            int lost = preEnergyPlayer - activeMonster.getEnergy();
                            NotificationManager.showDamage(playerCell, lost);
                            AnimationManager.animateDamageShake(playerCell);
                        }

                        // EVENT: Card Drawn
                        if (gameEngine.getBoard().getBoardCells()[activeMonster.getPosition() / 10][activeMonster.getPosition() % 10] instanceof game.engine.cells.CardCell) {
                            SoundManager.playCard();
                            AnimationManager.showCardPopup(rootPane, "MYSTERY CARD", "A card effect has been triggered!");
                        }

                        // Check for winner sound
                        if (gameEngine.getWinner() != null) {
                            SoundManager.playVictory();
                        }

                        // FINALIZE TURN
                        checkWinCondition();
                        if (gameEngine.getWinner() == null) {
                            btnRollDice.setDisable(false);
                        }
                    });

                } catch (InvalidMoveException ex) {
                    SoundManager.playError();
                    showError("Invalid Move", ex.getMessage());
                    btnRollDice.setDisable(false); // Re-enable if error
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        btnPowerup.setOnAction(e -> {
            if (gameEngine == null) return;
            try {
                gameEngine.usePowerup();
                SoundManager.playCard(); // Re-use card sound for powerup
                log(gameEngine.getCurrent().getName() + " used a powerup!");
                StackPane playerCell = boardRenderer.getCellVisual(gameEngine.getCurrent().getPosition());
                AnimationManager.animateFloatingText(playerCell, "-500 ENERGY", false);
            } catch (OutOfEnergyException ex) {
                SoundManager.playError();
                showError("Not Enough Energy", ex.getMessage());
            }
            updateUI();
        });

        if (HomeMenu != null) {
            HomeMenu.setOnAction(e -> {
                // Ensure UI sound plays before leaving
                SoundManager.playMove();
                SceneManager.switchScene("MainMenu.fxml");
            });
        }
    }

    // Keep this one so basic calls still work!
    private void updateUI() {
        updateUI(null);
    }

    // Replace the main updateUI method with this:
    private void updateUI(Runnable onComplete) {
        if (gameEngine == null) return;

        Monster player = gameEngine.getPlayer();
        Monster opponent = gameEngine.getOpponent();
        Monster current = gameEngine.getCurrent();

        // --- DYNAMIC TURN INDICATOR ---
        if (current == player) {
            lblTurnInfo.setText("TURN: " + player.getName().toUpperCase() + " (YOU)");
            lblTurnInfo.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 32px; -fx-font-weight: bold;"); // Blue for Player
        } else {
            lblTurnInfo.setText("TURN: " + opponent.getName().toUpperCase() + " (OPPONENT)");
            lblTurnInfo.setStyle("-fx-text-fill: #ff003c; -fx-font-size: 32px; -fx-font-weight: bold;"); // Red for Opponent
        }

        // --- UPDATE STATS PANELS USING THE RESTORED RPG HELPER ---
        updateStatPanel(player, lblPlayerStats, "--- YOUR MONSTER ---");
        updateStatPanel(opponent, lblOpponentStats, "--- OPPONENT ---");

        // Pass the callback to the renderer for the step-by-step animation!
        boardRenderer.updateMonsterPositions(player, opponent, current, onComplete);
    }



    private void log(String message) {
        if (actionLog != null) actionLog.appendText(message + "\n");
    }

    private void showError(String title, String message) {
        // Platform.runLater ensures the dialog waits until the animation pulse is over
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Milestone 3: The game should not be stopped / terminated
            // for any invalid action
            alert.showAndWait();
        });
    }

    private void setupVisuals() {
        bgImage.setPreserveRatio(false);
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        try {
            URL url = getClass().getResource("/MainMenu/assets/images/background.jpg");
            if (url != null) bgImage.setImage(new Image(url.toExternalForm()));
        } catch (Exception e) { System.out.println("Background image not found."); }

        Platform.runLater(this::createParticles);
    }

    private void createParticles() {
        particlePane.setPrefSize(rootPane.getWidth(), rootPane.getHeight());
        Random rand = new Random();
        double w = rootPane.getWidth() > 0 ? rootPane.getWidth() : 1000;
        double h = rootPane.getHeight() > 0 ? rootPane.getHeight() : 800;

        for (int i = 0; i < 40; i++) {
            Circle p = new Circle(rand.nextDouble() * 5 + 2, Color.web("#00d4ff", 0.5));
            p.setEffect(new GaussianBlur(rand.nextDouble() * 5 + 5));
            p.setTranslateX(rand.nextDouble() * w);
            p.setTranslateY(h + (rand.nextDouble() * 200));
            particlePane.getChildren().add(p);

            TranslateTransition floatUp = new TranslateTransition(Duration.seconds(rand.nextDouble() * 10 + 10), p);
            floatUp.setByY(-(h + 400));
            floatUp.setCycleCount(Animation.INDEFINITE);

            FadeTransition pulse = new FadeTransition(Duration.seconds(rand.nextDouble() * 2 + 2), p);
            pulse.setFromValue(0.2); pulse.setToValue(0.8);
            pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);

            floatUp.play(); pulse.play();
        }
    }

    // Inside GameController.java

    private void checkWinCondition() {
        Monster winner = gameEngine.getWinner();

        if (winner != null) {
            log("GAME OVER! " + winner.getName() + " WINS!");

            // Lock the game controls
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);

            // FORCE the new window onto the main JavaFX Application Thread safely
            Platform.runLater(() -> {
                Stage currentStage = (Stage) boardGrid.getScene().getWindow();
                WinWindow.display(winner, currentStage);
            });
        }
    }

// =========================================
    // RPG STATS HELPER METHODS
    // =========================================

    private String getMonsterType(Monster m) {
        // Gets the class name (e.g., "Dasher", "Dynamo") dynamically
        return m.getClass().getSimpleName();
    }

    private String getActiveEffects(Monster m) {
        StringBuilder effects = new StringBuilder();

        if (m.isShielded()) effects.append("[SHIELDED] ");
        if (m.isFrozen()) effects.append("[FROZEN] ");
        if (m.getConfusionTurns() > 0) {
            effects.append("[CONFUSED: ").append(m.getConfusionTurns()).append(" turns] ");
        }

        return effects.length() == 0 ? "None" : effects.toString();
    }

    private void updateStatPanel(Monster m, Label displayLabel, String title) {
        String originalRole = m.getOriginalRole().toString();
        String currentRole = m.getRole().toString();

        // Check if roles differ (indicates confusion)
        String roleDisplay = originalRole;
        if (!originalRole.equals(currentRole)) {
            roleDisplay = originalRole + " (Confused as " + currentRole + ")";
        }

        // Build the multiline string
        String stats = String.format(
                "%s\n" +
                        "Name: %s\n" +
                        "Base Role: %s\n" +
                        "Type: %s\n" +
                        "Energy: %d\n" +
                        "Position: %d\n" +
                        "Effects: %s",
                title,
                m.getName(),
                roleDisplay,
                getMonsterType(m),
                m.getEnergy(),
                m.getPosition(),
                getActiveEffects(m)
        );

        displayLabel.setText(stats);
    }

}