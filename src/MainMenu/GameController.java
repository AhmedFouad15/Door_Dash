package MainMenu;

import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
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
    @FXML private MediaView diceVideo;

    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer diceMediaPlayer;

    // The Engines
    private Game gameEngine;
    private BoardRenderer boardRenderer;

    public static Role playerRole = Role.SCARER;

    public void initialize() {
        SoundManager.init();
        setupVisuals();

        try {
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

        try {
            // Load background music (loops forever)
            URL musicUrl = getClass().getResource("/MainMenu/assets/audio/background2.mp3");
            if (musicUrl != null) {
                Media bgMedia = new Media(musicUrl.toExternalForm());
                backgroundMusicPlayer = new MediaPlayer(bgMedia);
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusicPlayer.setVolume(0.3);
                backgroundMusicPlayer.play();
            }
        } catch (Exception e) {
            System.out.println("Could not load background audio file!");
        }
    }

    private void setupActions() {
        btnRollDice.setOnAction(e -> {
            if (gameEngine == null) return;
            btnRollDice.setDisable(true);
            SoundManager.playDice();

            Monster activeMonster = gameEngine.getCurrent();
            Monster inactiveMonster = (activeMonster == gameEngine.getPlayer()) ? gameEngine.getOpponent() : gameEngine.getPlayer();

            // --- 1. RECORD TRUE PRE-TURN STATE ---
            int prePosActive = activeMonster.getPosition();
            int preEnergyActive = activeMonster.getEnergy();
            int prePosInactive = inactiveMonster.getPosition();
            int preEnergyInactive = inactiveMonster.getEnergy();
            boolean preShieldActive = activeMonster.isShielded();
            game.engine.Role preRoleActive = activeMonster.getRole();

            // Safely peek at the top card
            if (game.engine.Board.getCards().isEmpty()) {
                game.engine.Board.reloadCards();
            }
            game.engine.cards.Card topCard = game.engine.Board.getCards().get(0);

            AnimationManager.animateDiceRoll(btnRollDice, () -> {
                try {
                    gameEngine.playTurn();

                    // --- 2. DETECTIVE WORK: FIND EXACTLY WHERE THEY LANDED ---
                    int postPosActive = activeMonster.getPosition();
                    int roll = -1;

                    for (int r = 1; r <= 6; r++) {
                        if (postPosActive == (prePosActive + r) % 100) { roll = r; break; }
                    }
                    if (roll == -1) {
                        for (int r = 1; r <= 6; r++) {
                            Cell cell = getCellAt((prePosActive + r) % 100);
                            if (cell instanceof ContaminationSock && postPosActive < (prePosActive + r) % 100) { roll = r; break; }
                            if (cell instanceof game.engine.cells.ConveyorBelt && postPosActive > (prePosActive + r) % 100) { roll = r; break; }
                            if (cell instanceof game.engine.cells.CardCell) { roll = r; break; }
                        }
                    }
                    if (roll == -1) roll = Math.max(1, (postPosActive - prePosActive + 100) % 100);

                    final int finalRoll = roll;
                    final int landedPos = (prePosActive + finalRoll) % 100;
                    final Cell landedCell = getCellAt(landedPos);

                    log("=================================");
                    log("🎲 " + activeMonster.getName() + " rolled a " + finalRoll + ".");

                    // ==========================================
                    // 🎬 PLAY VIDEO FIRST!
                    // ==========================================
                    playDiceVideo(finalRoll, () -> {
                        SoundManager.playMove(); // Play walking sound after video finishes

                        // ==========================================
                        // TIME STOP: UI-ONLY DUMMY MONSTERS
                        // ==========================================
                        Monster uiActive = new Monster(activeMonster.getName(), "", activeMonster.getOriginalRole(), preEnergyActive) {
                            @Override public void executePowerupEffect(Monster opp) {}
                        };
                        uiActive.setPosition(landedPos);
                        uiActive.setRole(activeMonster.getRole());

                        Monster uiInactive = new Monster(inactiveMonster.getName(), "", inactiveMonster.getOriginalRole(), preEnergyInactive) {
                            @Override public void executePowerupEffect(Monster opp) {}
                        };
                        uiInactive.setPosition(prePosInactive);
                        uiInactive.setRole(inactiveMonster.getRole());

                        Monster uiPlayer = (activeMonster == gameEngine.getPlayer()) ? uiActive : uiInactive;
                        Monster uiOpponent = (activeMonster == gameEngine.getOpponent()) ? uiActive : uiInactive;

                        // Phase 1: Animate to the cell using dummy objects
                        boardRenderer.updateMonsterPositions(uiPlayer, uiOpponent, uiActive, () -> {

                            if (landedCell instanceof game.engine.cells.CardCell) {
                                log("🃏 Landed on a Mystery Card!");
                                log("   -> Drew [" + topCard.getName() + "]: " + topCard.getDescription());
                                SoundManager.playCard();

                                AnimationManager.showCardPopup(rootPane, topCard.getName(), topCard.getDescription(), () -> {
                                    // Phase 2: Card Closed! Now let the REAL backend models animate their final jumps
                                    updateUI(() -> {
                                        finalizeTurn(activeMonster, inactiveMonster, prePosActive, preEnergyActive, prePosInactive, preEnergyInactive, preShieldActive, preRoleActive, landedCell, finalRoll);
                                    });
                                });
                            } else {
                                // Not a card cell? Just update normally using the real models.
                                updateUI(() -> {
                                    finalizeTurn(activeMonster, inactiveMonster, prePosActive, preEnergyActive, prePosInactive, preEnergyInactive, preShieldActive, preRoleActive, landedCell, finalRoll);
                                });
                            }
                        });
                    });

                } catch (InvalidMoveException ex) {
                    SoundManager.playError();
                    showError("Invalid Move", ex.getMessage());
                    btnRollDice.setDisable(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        btnPowerup.setOnAction(e -> {
            if (gameEngine == null) return;
            try {
                gameEngine.usePowerup();
                SoundManager.playCard();
                log("=================================");
                log("⚡ " + gameEngine.getCurrent().getName() + " ACTIVATED A POWER-UP!");
                log("   -> Cost: 500 Energy.");
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
                SoundManager.playMove();
                SceneManager.switchScene("MainMenu.fxml");
                stopAudio();
            });
        }
    }

    // =========================================
    // HELPER METHODS: TURN LOGIC & BOARD
    // =========================================

    private void finalizeTurn(Monster activeMonster, Monster inactiveMonster, int prePosActive, int preEnergyActive, int prePosInactive, int preEnergyInactive, boolean preShieldActive, game.engine.Role preRoleActive, Cell landedCell, int roll) {

        int finalPos = activeMonster.getPosition();
        int energyDiff = activeMonster.getEnergy() - preEnergyActive;
        int oppEnergyDiff = inactiveMonster.getEnergy() - preEnergyInactive;

        if (landedCell instanceof ContaminationSock) {
            log("🧦 Oh no! Stepped on a Contamination Sock!");
            SoundManager.playLose();
        } else if (landedCell instanceof game.engine.cells.ConveyorBelt) {
            log("⚙️ Swoosh! Rode a Conveyor Belt!");
        } else if (landedCell instanceof game.engine.cells.DoorCell) {
            log("🚪 Interacted with a Door Cell.");
        } else if (landedCell instanceof game.engine.cells.MonsterCell) {
            log("👾 Encountered a Stationed Monster!");
        }

        if (finalPos != (prePosActive + roll) % 100) {
            log("   -> Shifted to final cell: " + finalPos);
        }

        if (energyDiff > 0) log("   -> 🟢 Gained " + energyDiff + " Energy.");
        else if (energyDiff < 0) log("   -> 🔴 Lost " + Math.abs(energyDiff) + " Energy.");

        if (oppEnergyDiff < 0) log("   -> ⚔️ " + inactiveMonster.getName() + " was hit and lost " + Math.abs(oppEnergyDiff) + " Energy!");

        if (preShieldActive && !activeMonster.isShielded()) {
            log("   -> 🛡️ Shield absorbed a negative impact!");
        }
        if (!preRoleActive.equals(activeMonster.getRole())) {
            log("   -> 💫 " + activeMonster.getName() + " became CONFUSED!");
        }
        log("=================================");

        if (energyDiff < 0) {
            StackPane playerCell = boardRenderer.getCellVisual(activeMonster.getPosition());
            SoundManager.playDamage();
            NotificationManager.showDamage(playerCell, Math.abs(energyDiff));
            AnimationManager.animateDamageShake(playerCell);
        }

        if (oppEnergyDiff < 0) {
            StackPane oppCell = boardRenderer.getCellVisual(inactiveMonster.getPosition());
            SoundManager.playDamage();
            NotificationManager.showDamage(oppCell, Math.abs(oppEnergyDiff));
            AnimationManager.animateDamageShake(oppCell);
        }

        if (gameEngine.getWinner() != null) {
            SoundManager.playVictory();
        }

        checkWinCondition();
        if (gameEngine.getWinner() == null) {
            btnRollDice.setDisable(false);
        }
    }

    private Cell getCellAt(int index) {
        int row = index / 10;
        int col = index % 10;
        if (row % 2 == 1) {
            col = 9 - col;
        }
        return gameEngine.getBoard().getBoardCells()[row][col];
    }

    // =========================================
    // HELPER METHODS: UI & VISUALS
    // =========================================

    private void updateUI() {
        updateUI(null);
    }

    private void updateUI(Runnable onComplete) {
        if (gameEngine == null) return;

        Monster player = gameEngine.getPlayer();
        Monster opponent = gameEngine.getOpponent();
        Monster current = gameEngine.getCurrent();

        if (current == player) {
            lblTurnInfo.setText("TURN: " + player.getName().toUpperCase() + " (YOU)");
            lblTurnInfo.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 32px; -fx-font-weight: bold;");
        } else {
            lblTurnInfo.setText("TURN: " + opponent.getName().toUpperCase() + " (OPPONENT)");
            lblTurnInfo.setStyle("-fx-text-fill: #ff003c; -fx-font-size: 32px; -fx-font-weight: bold;");
        }

        updateStatPanel(player, lblPlayerStats, "--- YOUR MONSTER ---");
        updateStatPanel(opponent, lblOpponentStats, "--- OPPONENT ---");

        boardRenderer.updateMonsterPositions(player, opponent, current, onComplete);
    }

    private void playDiceVideo(int roll, Runnable onFinished) {
        try {
            String videoPath = "/MainMenu/assets/video/" + roll + ".mp4";
            URL videoURL = getClass().getResource(videoPath);

            if (videoURL == null) {
                System.out.println("Video not found: " + videoPath);
                if (onFinished != null) onFinished.run();
                return;
            }

            if (diceMediaPlayer != null) diceMediaPlayer.stop();

            Media media = new Media(videoURL.toExternalForm());
            diceMediaPlayer = new MediaPlayer(media);
            diceVideo.setMediaPlayer(diceMediaPlayer);
            diceVideo.setVisible(true);

            diceMediaPlayer.setOnEndOfMedia(() -> {
                diceVideo.setVisible(false);
                if (onFinished != null) onFinished.run();
            });

            diceMediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
            if (onFinished != null) onFinished.run();
        }
    }

    private void log(String message) {
        if (actionLog != null) actionLog.appendText(message + "\n");
    }

    private void showError(String title, String message) {
        // Platform.runLater ensures the dialog waits until the animation pulse is over
        Platform.runLater(() -> {
            // 1. Dark Blur Overlay to dim the board
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.75);");

            // 2. Square Card Container
            // Styled to look like a mechanical board cell but sized as an interactive card (320x320 square)
            VBox squareCard = new VBox(20);
            squareCard.setAlignment(javafx.geometry.Pos.CENTER);
            squareCard.setPrefWidth(320);
            squareCard.setPrefHeight(320);
            squareCard.setMaxWidth(320);
            squareCard.setMaxHeight(320);

            // Clean, sharp square borders with glowing crimson outline matching the game's theme
            squareCard.setStyle(
                    "-fx-background-color: #1e1112; " + // Deep matching dark red background
                            "-fx-border-color: #ff003c; " +     // Outlined crimson border
                            "-fx-border-width: 4; " +
                            "-fx-background-radius: 0; " +      // 0 radius forces a perfect square cell shape
                            "-fx-border-radius: 0; " +
                            "-fx-padding: 25;"
            );

            // Apply the glowing drop-shadow effect used elsewhere in your animations
            squareCard.setEffect(new javafx.scene.effect.DropShadow(25, javafx.scene.paint.Color.web("#ff003c")));

            // 3. Header Text
            Label lblTitle = new Label(title.toUpperCase());
            lblTitle.setStyle("-fx-text-fill: #ff003c; -fx-font-size: 22px; -fx-font-weight: bold; -fx-alignment: center;");
            lblTitle.setWrapText(true);

            // 4. Body Content Description
            Label lblMessage = new Label(message);
            lblMessage.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: normal;");
            lblMessage.setWrapText(true);
            lblMessage.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            // 5. Interactive Confirmation Action Button
            Button btnDismiss = new Button("OK");
            btnDismiss.setPrefWidth(140);
            btnDismiss.setPrefHeight(35);
            btnDismiss.setStyle(
                    "-fx-background-color: #ff003c; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-background-radius: 0; " + // Squared button to match card aesthetic
                            "-fx-cursor: hand;"
            );

            // Visual Hover state changes
            btnDismiss.setOnMouseEntered(ev -> btnDismiss.setStyle("-fx-background-color: #ff3366; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 0; -fx-cursor: hand;"));
            btnDismiss.setOnMouseExited(ev -> btnDismiss.setStyle("-fx-background-color: #ff003c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 0; -fx-cursor: hand;"));

            // 6. Assemble layout structures
            squareCard.getChildren().addAll(lblTitle, lblMessage, btnDismiss);
            overlay.getChildren().add(squareCard);
            rootPane.getChildren().add(overlay);

            // 7. Entry Transition Animation (Pop-In)
            squareCard.setScaleX(0);
            squareCard.setScaleY(0);
            javafx.animation.ScaleTransition popIn = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), squareCard);
            popIn.setToX(1.0);
            popIn.setToY(1.0);
            popIn.play();

            // 8. Close Transition Animation on interaction click
            btnDismiss.setOnAction(clickEvent -> {
                btnDismiss.setDisable(true); // Stop double clicking side effects
                javafx.animation.ScaleTransition popOut = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), squareCard);
                popOut.setToX(0);
                popOut.setToY(0);
                popOut.setOnFinished(finishEvent -> {
                    rootPane.getChildren().remove(overlay); // Clears overlay smoothly from screen layout hierarchy
                });
                popOut.play();
            });

            // Consume mouse events to prevent clicking background cells while popup is active
            overlay.setOnMouseClicked(mouseEvent -> mouseEvent.consume());
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

    private void checkWinCondition() {
        Monster winner = gameEngine.getWinner();

        if (winner != null) {
            log("GAME OVER! " + winner.getName() + " WINS!");
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);

            Platform.runLater(() -> {
                Stage currentStage = (Stage) boardGrid.getScene().getWindow();
                WinWindow.display(winner, currentStage);
            });
        }
    }

    private String getMonsterType(Monster m) {
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

        String roleDisplay = originalRole;
        if (!originalRole.equals(currentRole)) {
            roleDisplay = originalRole + " (Confused as " + currentRole + ")";
        }

        String stats = String.format(
                "%s\nName: %s\nBase Role: %s\nType: %s\nEnergy: %d\nPosition: %d\nEffects: %s",
                title, m.getName(), roleDisplay, getMonsterType(m), m.getEnergy(), m.getPosition(), getActiveEffects(m)
        );

        displayLabel.setText(stats);
    }

    public void stopAudio() {
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();
        if (diceMediaPlayer != null) diceMediaPlayer.stop();
    }
}