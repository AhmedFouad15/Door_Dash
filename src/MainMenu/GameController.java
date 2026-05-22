package MainMenu;

import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cards.StartOverCard;
import game.engine.cards.SwapperCard;
import game.engine.cells.*;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private Pane particlePane;
    @FXML private Pane specialPathPane;

    @FXML private GridPane boardGrid;
    @FXML private StackPane boardCameraPane;
    @FXML private Label lblTurnInfo;
    @FXML private VBox playerStatsBox;
    @FXML private Label lblPlayerStats;
    @FXML private Label lblOpponentStats;
    @FXML private Label lblDeckStatus;
    @FXML private Label lblStationedMonsters;
    @FXML private TextArea actionLog;

    @FXML private Button btnRollDice;
    @FXML private Button btnPowerup;
    @FXML private Button btnStationedDetails;
    @FXML private Button HomeMenu;
    @FXML private Label diceFaceLabel;

    private MediaPlayer backgroundMusicPlayer;
    private PauseTransition specialPathRedrawDelay;
    private PauseTransition computerTurnDelay;
    private Monster singlePlayerTurnLabelOverride;
    private static final double MOVE_CAMERA_ZOOM = 1.45;
    private static final Image CHAR1_IMAGE = loadGameImage("/MainMenu/assets/images/char1.png");
    private static final Image CHAR2_IMAGE = loadGameImage("/MainMenu/assets/images/char2.png");
    private static final Image CHAR3_IMAGE = loadGameImage("/MainMenu/assets/images/char3.png");
    private static final Image SHIELD_IMAGE = loadGameImage("/MainMenu/assets/images/shield.png");
    private static final Image TELEPORT_IMAGE = loadGameImage("/MainMenu/assets/images/teleport.png");

    // The Engines
    private Game gameEngine;
    private BoardRenderer boardRenderer;

    public static Role playerRole = Role.SCARER;
    public static boolean singlePlayerMode = false;
    public static String playerDisplayName = "You";
    public static String opponentDisplayName = "Opponent";

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
        setupSpecialPathDrawing();

        setupActions();
        setupSpaceShortcut();
        updateUI();
        updateTurnControls();

        try {
            // Load background music (loops forever)
            URL musicUrl = getClass().getResource("/MainMenu/assets/audio/background2.mp3");
            if (musicUrl != null) {
                Media bgMedia = new Media(musicUrl.toExternalForm());
                backgroundMusicPlayer = new MediaPlayer(bgMedia);
                backgroundMusicPlayer.setVolume(0.3);
                backgroundMusicPlayer.setOnReady(() -> {
                    backgroundMusicPlayer.seek(Duration.seconds(3));
                    backgroundMusicPlayer.play();
                });
                backgroundMusicPlayer.setOnEndOfMedia(() -> {
                    backgroundMusicPlayer.seek(Duration.seconds(3));
                    backgroundMusicPlayer.play();
                });
            }
        } catch (Exception e) {
            System.out.println("Could not load background audio file!");
        }
    }

    private void setupSpaceShortcut() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        event.consume();
                        if (!btnRollDice.isDisabled()) {
                            btnRollDice.fire();
                        }
                    } else if (event.isControlDown() && event.getCode() == KeyCode.W) {
                        event.consume();
                        makeCurrentPlayerWin();
                    }
                });
            }
        });
    }

    private void setupActions() {
        btnRollDice.setOnAction(e -> {
            if (gameEngine == null) return;
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);
            SoundManager.playDice();

            Monster activeMonster = gameEngine.getCurrent();
            Monster inactiveMonster = (activeMonster == gameEngine.getPlayer()) ? gameEngine.getOpponent() : gameEngine.getPlayer();
            if (singlePlayerMode) {
                singlePlayerTurnLabelOverride = activeMonster;
            }

            // --- 1. RECORD TRUE PRE-TURN STATE ---
            int prePosActive = activeMonster.getPosition();
            int preEnergyActive = activeMonster.getEnergy();
            int prePosInactive = inactiveMonster.getPosition();
            int preEnergyInactive = inactiveMonster.getEnergy();
            boolean preShieldActive = activeMonster.isShielded();
            boolean preShieldInactive = inactiveMonster.isShielded();
            boolean preFrozenActive = activeMonster.isFrozen();
            game.engine.Role preRoleActive = activeMonster.getRole();
            game.engine.Role preRoleInactive = inactiveMonster.getRole();
            int preConfusionActive = activeMonster.getConfusionTurns();
            int preConfusionInactive = inactiveMonster.getConfusionTurns();
            int preMomentumTurns = activeMonster instanceof Dasher ? ((Dasher) activeMonster).getMomentumTurns() : 0;
            int preFocusTurns = activeMonster instanceof MultiTasker ? ((MultiTasker) activeMonster).getNormalSpeedTurns() : 0;
            Map<Monster, Integer> preStationedEnergy = snapshotStationedMonsterEnergy();

            // Safely peek at the top card
            if (game.engine.Board.getCards().isEmpty()) {
                game.engine.Board.reloadCards();
            }
            Card topCard = game.engine.Board.getCards().get(0);

            AnimationManager.animateDiceRoll(btnRollDice, () -> {
                try {
                    gameEngine.playTurn();

                    if (preFrozenActive) {
                        log("=================================");
                        log(activeMonster.getName() + " was FROZEN and skipped the turn.");
                        log("Freeze expired. Turn passed to " + gameEngine.getCurrent().getName() + ".");
                        log("=================================");
                        singlePlayerTurnLabelOverride = null;
                        updateUI();
                        finishTurnControls();
                        return;
                    }

                    // --- 2. DETECTIVE WORK: FIND EXACTLY WHERE THEY LANDED ---
                    int postPosActive = activeMonster.getPosition();
                    TurnGuess turnGuess = inferTurnGuess(activeMonster, inactiveMonster, prePosActive, prePosInactive, postPosActive, preMomentumTurns, preFocusTurns, topCard);

                    final int finalRoll = turnGuess.roll;
                    final int effectiveMove = turnGuess.effectiveMove;
                    final int landedPos = turnGuess.landedPosition;
                    final Cell landedCell = turnGuess.landedCell;
                    log("=================================");
                    log("🎲 " + activeMonster.getName() + " rolled a " + finalRoll + ".");
                    if (effectiveMove != finalRoll) {
                        log("   -> " + getMonsterType(activeMonster) + " movement changed that to " + effectiveMove + " cells.");
                    }

                    // ==========================================
                    // 🎬 PLAY VIDEO FIRST!
                    // ==========================================
                    playDiceFaceAnimation(finalRoll, () -> {
                        SoundManager.playMove(); // Play walking sound after video finishes
                        focusMoveCameraOnPosition(prePosActive, 220);

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
                        boolean uiActiveIsPlayer = activeMonster == gameEngine.getPlayer();
                        boardRenderer.updateMonsterPositions(uiPlayer, uiOpponent, uiActive, uiActiveIsPlayer, this::focusMoveCameraOnPosition, () -> {

                            if (landedCell instanceof game.engine.cells.CardCell) {
                                log("🃏 Landed on a Mystery Card!");
                                log("   -> Drew [" + topCard.getName() + "]: " + topCard.getDescription());
                                SoundManager.playCard();

                                AnimationManager.showCardPopup(rootPane, topCard.getName(), topCard.getDescription(), () -> {
                                    // Phase 2: Card Closed! Now let the REAL backend models animate their final jumps
                                    focusMoveCameraOnPosition(landedPos, 180);
                                    updateUIFollowingMonster(activeMonster, () -> {
                                        finalizeTurn(activeMonster, inactiveMonster, prePosActive, preEnergyActive, prePosInactive, preEnergyInactive, preShieldActive, preShieldInactive, preRoleActive, preRoleInactive, preConfusionActive, preConfusionInactive, preStationedEnergy, landedCell, landedPos, finalRoll, effectiveMove);
                                    });
                                });
                            } else {
                                if (isTransportLanding(landedCell, activeMonster.getPosition(), landedPos)) {
                                    updateStatusPanelsOnly();
                                    boolean activeIsPlayer = activeMonster == gameEngine.getPlayer();
                                    Runnable glideToFinalCell = () -> boardRenderer.updateMonsterPositionsWithDirectGlide(gameEngine.getPlayer(), gameEngine.getOpponent(), activeMonster, activeIsPlayer, this::focusMoveCameraOnPosition, () -> {
                                        finalizeTurn(activeMonster, inactiveMonster, prePosActive, preEnergyActive, prePosInactive, preEnergyInactive, preShieldActive, preShieldInactive, preRoleActive, preRoleInactive, preConfusionActive, preConfusionInactive, preStationedEnergy, landedCell, landedPos, finalRoll, effectiveMove);
                                    });

                                    if (landedCell instanceof ContaminationSock) {
                                        playContaminationSequence(landedPos, glideToFinalCell);
                                    } else if (landedCell instanceof ConveyorBelt) {
                                        playConveyorTeleportSequence(landedPos, activeMonster.getPosition(), activeMonster, uiActiveIsPlayer, () -> {
                                            refreshBoardNow();
                                            finalizeTurn(activeMonster, inactiveMonster, prePosActive, preEnergyActive, prePosInactive, preEnergyInactive, preShieldActive, preShieldInactive, preRoleActive, preRoleInactive, preConfusionActive, preConfusionInactive, preStationedEnergy, landedCell, landedPos, finalRoll, effectiveMove);
                                        });
                                    } else {
                                        glideToFinalCell.run();
                                    }
                                } else {
                                    // Not a card or transport cell? Just update normally using the real models.
                                    playDoorUseSequenceIfNeeded(landedCell, landedPos, activeMonster, () -> updateUIFollowingMonster(activeMonster, () -> {
                                        finalizeTurn(activeMonster, inactiveMonster, prePosActive, preEnergyActive, prePosInactive, preEnergyInactive, preShieldActive, preShieldInactive, preRoleActive, preRoleInactive, preConfusionActive, preConfusionInactive, preStationedEnergy, landedCell, landedPos, finalRoll, effectiveMove);
                                    }));
                                }
                            }
                        });
                    });

                } catch (InvalidMoveException ex) {
                    SoundManager.playError();
                    showError("Invalid Move", ex.getMessage());
                    finishTurnControls();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            if(activeMonster instanceof Dasher){
                System.out.println("Dasher");
            }
            else if(activeMonster instanceof Dynamo){
                System.out.println("Dynamo");
            }
            else if(activeMonster instanceof MultiTasker){
                System.out.println("MultiTasker");
            }
            else if(activeMonster instanceof Schemer){
                System.out.println("Schemer");
            }
            else{
                System.out.println("Unknown Monster");
            }
        });

        btnPowerup.setOnAction(e -> {
            if (gameEngine == null) return;
            try {
                Monster current = gameEngine.getCurrent();
                String powerupName = getMonsterPowerupName(current);
                Map<Monster, Integer> preStationedEnergy = snapshotStationedMonsterEnergy();
                gameEngine.usePowerup();
                SoundManager.playCard();
                log("=================================");
                log("⚡ " + current.getName() + " activated " + powerupName + "!");
                log("   -> Cost: 500 Energy.");
                log("   -> " + getMonsterPowerupStatus(current));
                logStationedMonsterEnergyChanges(preStationedEnergy);
                StackPane playerCell = boardRenderer.getCellVisual(current.getPosition());
                AnimationManager.animatePowerupActivate(playerCell, powerupName);
                AnimationManager.animateFloatingText(playerCell, "-500 ENERGY", false);
            } catch (OutOfEnergyException ex) {
                SoundManager.playError();
                showError("Not Enough Energy", ex.getMessage());
            }
            updateUI();
            updateTurnControls();
        });

        if (btnStationedDetails != null) {
            btnStationedDetails.setOnAction(e -> showStationedMonstersPopup());
        }

        if (HomeMenu != null) {
            HomeMenu.setOnAction(e -> {
                if (computerTurnDelay != null) {
                    computerTurnDelay.stop();
                }
                SoundManager.playMove();
                stopAudio();
                SceneManager.switchScene("MainMenu.fxml");
            });
        }
    }

    // =========================================
    // HELPER METHODS: TURN LOGIC & BOARD
    // =========================================

    private void finalizeTurn(Monster activeMonster, Monster inactiveMonster, int prePosActive, int preEnergyActive,
                              int prePosInactive, int preEnergyInactive, boolean preShieldActive, boolean preShieldInactive,
                              game.engine.Role preRoleActive, game.engine.Role preRoleInactive, int preConfusionActive,
                              int preConfusionInactive, Map<Monster, Integer> preStationedEnergy, Cell landedCell,
                              int landedPos, int roll, int effectiveMove) {

        int finalPos = activeMonster.getPosition();
        int energyDiff = activeMonster.getEnergy() - preEnergyActive;
        int oppEnergyDiff = inactiveMonster.getEnergy() - preEnergyInactive;

        log("   -> Landed on cell " + landedPos + " (" + getCellDisplayName(landedCell) + ").");

        if (landedCell instanceof ContaminationSock) {
            log("🧦 Oh no! Stepped on a Contamination Sock!");
        } else if (landedCell instanceof ConveyorBelt) {
            log("⚙️ Swoosh! Rode a Conveyor Belt!");
        } else if (landedCell instanceof DoorCell) {
            DoorCell door = (DoorCell) landedCell;
            log("🚪 Interacted with a " + door.getRole() + " Door worth " + door.getEnergy() + " energy.");
            log("   -> Door is now " + (door.isActivated() ? "exhausted." : "still available."));
        } else if (landedCell instanceof MonsterCell) {
            MonsterCell monsterCell = (MonsterCell) landedCell;
            log("👾 Encountered stationed monster " + monsterCell.getCellMonster().getName() + ".");
        }

        if (finalPos != landedPos) {
            log("   -> Shifted to final cell: " + finalPos);
        }

        if (energyDiff > 0) log("   -> 🟢 Gained " + energyDiff + " Energy.");
        else if (energyDiff < 0) log("   -> 🔴 Lost " + Math.abs(energyDiff) + " Energy.");

        if (oppEnergyDiff > 0) log("   -> " + inactiveMonster.getName() + " gained " + oppEnergyDiff + " Energy.");
        if (oppEnergyDiff < 0) log("   -> ⚔️ " + inactiveMonster.getName() + " was hit and lost " + Math.abs(oppEnergyDiff) + " Energy!");

        if (preShieldActive && !activeMonster.isShielded()) {
            log("   -> 🛡️ Shield absorbed a negative impact!");
        }
        if (preShieldInactive && !inactiveMonster.isShielded()) {
            log("   -> 🛡️ " + inactiveMonster.getName() + "'s shield was consumed.");
        }
        if (!preRoleActive.equals(activeMonster.getRole()) || activeMonster.getConfusionTurns() != preConfusionActive) {
            log("   -> 💫 " + activeMonster.getName() + " confusion: " + getConfusionStatus(activeMonster));
        }
        if (!preRoleInactive.equals(inactiveMonster.getRole()) || inactiveMonster.getConfusionTurns() != preConfusionInactive) {
            log("   -> 💫 " + inactiveMonster.getName() + " confusion: " + getConfusionStatus(inactiveMonster));
        }
        logStationedMonsterEnergyChanges(preStationedEnergy);
        logPowerupDuration(activeMonster);
        log("=================================");

        refreshBoardNow();
        resetMoveCamera();
        if (singlePlayerMode) {
            singlePlayerTurnLabelOverride = null;
            updateStatusPanelsOnly();
        }

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
            finishTurnControls();
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

    private TurnGuess inferTurnGuess(Monster activeMonster, Monster inactiveMonster, int prePosActive, int prePosInactive,
                                     int finalPosActive, int preMomentumTurns, int preFocusTurns, Card topCard) {
        TurnGuess fallback = null;

        for (int r = 1; r <= 6; r++) {
            int effectiveMove = getEffectiveMoveForRoll(activeMonster, r, preMomentumTurns, preFocusTurns);
            int landedPos = normalizePosition(prePosActive + effectiveMove);
            Cell landedCell = getCellAt(landedPos);
            TurnGuess guess = new TurnGuess(r, effectiveMove, landedPos, landedCell);

            if (fallback == null) {
                fallback = guess;
            }

            if (matchesFinalPosition(guess, prePosActive, prePosInactive, finalPosActive, inactiveMonster, topCard)) {
                return guess;
            }
        }

        return fallback != null ? fallback : new TurnGuess(1, 1, normalizePosition(prePosActive + 1), getCellAt(normalizePosition(prePosActive + 1)));
    }

    private boolean matchesFinalPosition(TurnGuess guess, int prePosActive, int prePosInactive, int finalPosActive,
                                         Monster inactiveMonster, Card topCard) {
        if (guess.landedCell instanceof ConveyorBelt) {
            return normalizePosition(guess.landedPosition + ((ConveyorBelt) guess.landedCell).getEffect()) == finalPosActive;
        }

        if (guess.landedCell instanceof ContaminationSock) {
            return normalizePosition(guess.landedPosition + ((ContaminationSock) guess.landedCell).getEffect()) == finalPosActive;
        }

        if (guess.landedCell instanceof CardCell) {
            if (topCard instanceof StartOverCard && !topCard.isLucky()) {
                return finalPosActive == Constants.STARTING_POSITION;
            }
            if (topCard instanceof SwapperCard && guess.landedPosition < prePosInactive) {
                return finalPosActive == prePosInactive;
            }
            return finalPosActive == guess.landedPosition;
        }

        return finalPosActive == guess.landedPosition;
    }

    private int getEffectiveMoveForRoll(Monster monster, int roll, int preMomentumTurns, int preFocusTurns) {
        if (monster instanceof Dasher) {
            return roll * (preMomentumTurns > 0 ? 3 : 2);
        }

        if (monster instanceof MultiTasker && preFocusTurns <= 0) {
            return roll / 2;
        }

        return roll;
    }

    private int normalizePosition(int position) {
        int normalized = position % Constants.BOARD_SIZE;
        return normalized < 0 ? normalized + Constants.BOARD_SIZE : normalized;
    }

    private void refreshBoardNow() {
        boardRenderer.refreshBoard(gameEngine.getBoard(), gameEngine.getPlayer(), gameEngine.getOpponent(), gameEngine.getCurrent());
        requestSpecialPathRedraw();
    }

    private boolean isTransportLanding(Cell landedCell, int finalPos, int landedPos) {
        return (landedCell instanceof ConveyorBelt || landedCell instanceof ContaminationSock) && finalPos != landedPos;
    }

    private void playContaminationSequence(int landedPos, Runnable onFinished) {
        StackPane sockCell = boardRenderer.getCellVisual(landedPos);
        if (sockCell == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        focusMoveCameraOnPosition(landedPos, 260);
        SoundManager.playContamination();

        HBox emergencyTeam = new HBox(-6);
        emergencyTeam.setMouseTransparent(true);
        emergencyTeam.setAlignment(Pos.CENTER);
        emergencyTeam.setOpacity(0);
        emergencyTeam.setTranslateX(-150);
        emergencyTeam.setTranslateY(-12);
        emergencyTeam.setId("contamination_emergency_team");
        emergencyTeam.getChildren().addAll(
                createEffectImageView(CHAR1_IMAGE, 42),
                createEffectImageView(CHAR2_IMAGE, 42),
                createEffectImageView(CHAR3_IMAGE, 42)
        );

        ImageView shield = createEffectImageView(SHIELD_IMAGE, 68);
        shield.setOpacity(0);
        shield.setScaleX(0.35);
        shield.setScaleY(0.35);
        shield.setId("contamination_shield");

        sockCell.getChildren().addAll(emergencyTeam, shield);
        emergencyTeam.toFront();
        shield.toFront();

        PauseTransition cameraHold = new PauseTransition(Duration.millis(350));
        cameraHold.setOnFinished(event -> {
            TranslateTransition teamMove = new TranslateTransition(Duration.millis(760), emergencyTeam);
            teamMove.setToX(0);
            teamMove.setToY(-12);

            FadeTransition teamFadeIn = new FadeTransition(Duration.millis(240), emergencyTeam);
            teamFadeIn.setToValue(1);

            ParallelTransition teamArrival = new ParallelTransition(teamMove, teamFadeIn);
            teamArrival.setOnFinished(arrived -> {
                ScaleTransition shieldPop = new ScaleTransition(Duration.millis(260), shield);
                shieldPop.setToX(1.0);
                shieldPop.setToY(1.0);

                FadeTransition shieldFadeIn = new FadeTransition(Duration.millis(220), shield);
                shieldFadeIn.setToValue(0.88);

                ParallelTransition shieldArrival = new ParallelTransition(shieldPop, shieldFadeIn);
                shieldArrival.setOnFinished(shielded -> {
                    PauseTransition shieldHold = new PauseTransition(Duration.millis(500));
                    shieldHold.setOnFinished(holdDone -> {
                        FadeTransition teamFadeOut = new FadeTransition(Duration.millis(260), emergencyTeam);
                        teamFadeOut.setToValue(0);

                        FadeTransition shieldFadeOut = new FadeTransition(Duration.millis(260), shield);
                        shieldFadeOut.setToValue(0);

                        ParallelTransition cleanup = new ParallelTransition(teamFadeOut, shieldFadeOut);
                        cleanup.setOnFinished(done -> {
                            sockCell.getChildren().removeAll(emergencyTeam, shield);
                            if (onFinished != null) onFinished.run();
                        });
                        cleanup.play();
                    });
                    shieldHold.play();
                });
                shieldArrival.play();
            });
            teamArrival.play();
        });
        cameraHold.play();
    }

    private void playConveyorTeleportSequence(int sourcePos, int destinationPos, Monster activeMonster,
                                              boolean activeIsPlayer, Runnable onFinished) {
        StackPane sourceCell = boardRenderer.getCellVisual(sourcePos);
        StackPane destinationCell = boardRenderer.getCellVisual(destinationPos);
        if (sourceCell == null || destinationCell == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        String activeVisualId = activeIsPlayer ? "player_visual" : "opponent_visual";
        Node monsterVisual = findChildById(sourceCell, activeVisualId);

        focusMoveCameraOnPosition(sourcePos, 260);
        animateTeleportDoor(sourceCell, true, monsterVisual, () -> {
            if (monsterVisual != null) {
                sourceCell.getChildren().remove(monsterVisual);
            }

            PauseTransition jumpPause = new PauseTransition(Duration.millis(180));
            jumpPause.setOnFinished(event -> {
                focusMoveCameraOnPosition(destinationPos, 320);

                PauseTransition cameraArrival = new PauseTransition(Duration.millis(360));
                cameraArrival.setOnFinished(arrived -> {
                    StackPane arrivalVisual = MonsterRenderer.createMonsterVisual(activeMonster, true);
                    arrivalVisual.setId(activeVisualId);
                    arrivalVisual.setOpacity(0);
                    arrivalVisual.setScaleX(0.45);
                    arrivalVisual.setScaleY(0.45);
                    destinationCell.getChildren().removeIf(node -> activeVisualId.equals(node.getId()));
                    destinationCell.getChildren().add(arrivalVisual);

                    animateTeleportDoor(destinationCell, false, arrivalVisual, onFinished);
                });
                cameraArrival.play();
            });
            jumpPause.play();
        });
    }

    private void animateTeleportDoor(StackPane cell, boolean entering, Node monsterVisual, Runnable onFinished) {
        ImageView portal = createEffectImageView(TELEPORT_IMAGE, 86);
        portal.setId("teleport_portal");
        portal.setOpacity(0);
        portal.setScaleX(0.18);
        portal.setScaleY(0.18);
        portal.setBlendMode(BlendMode.SCREEN);
        cell.getChildren().add(portal);
        portal.toFront();

        ScaleTransition openPortal = new ScaleTransition(Duration.millis(260), portal);
        openPortal.setToX(1.05);
        openPortal.setToY(1.05);

        FadeTransition fadePortalIn = new FadeTransition(Duration.millis(220), portal);
        fadePortalIn.setToValue(0.95);

        ParallelTransition open = new ParallelTransition(openPortal, fadePortalIn);
        open.setOnFinished(opened -> {
            ParallelTransition monsterMotion = new ParallelTransition();
            if (monsterVisual != null) {
                monsterVisual.toFront();
                if (entering) {
                    ScaleTransition shrink = new ScaleTransition(Duration.millis(360), monsterVisual);
                    shrink.setToX(0.15);
                    shrink.setToY(0.15);
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), monsterVisual);
                    fadeOut.setToValue(0);
                    monsterMotion.getChildren().addAll(shrink, fadeOut);
                } else {
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(260), monsterVisual);
                    fadeIn.setToValue(1);
                    ScaleTransition grow = new ScaleTransition(Duration.millis(360), monsterVisual);
                    grow.setToX(1.0);
                    grow.setToY(1.0);
                    monsterMotion.getChildren().addAll(fadeIn, grow);
                }
            }

            if (monsterMotion.getChildren().isEmpty()) {
                monsterMotion.getChildren().add(new PauseTransition(Duration.millis(260)));
            }

            monsterMotion.setOnFinished(moved -> {
                ScaleTransition closePortal = new ScaleTransition(Duration.millis(250), portal);
                closePortal.setToX(0.12);
                closePortal.setToY(0.12);

                FadeTransition fadePortalOut = new FadeTransition(Duration.millis(250), portal);
                fadePortalOut.setToValue(0);

                ParallelTransition close = new ParallelTransition(closePortal, fadePortalOut);
                close.setOnFinished(closed -> {
                    cell.getChildren().remove(portal);
                    if (onFinished != null) onFinished.run();
                });
                close.play();
            });
            monsterMotion.play();
        });
        open.play();
    }

    private Node findChildById(StackPane cell, String id) {
        if (cell == null || id == null) return null;
        for (Node child : cell.getChildren()) {
            if (id.equals(child.getId())) {
                return child;
            }
        }
        return null;
    }

    private ImageView createEffectImageView(Image image, double fitWidth) {
        ImageView imageView = new ImageView(image);
        imageView.setMouseTransparent(true);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(fitWidth);
        return imageView;
    }

    private void playDoorUseSequenceIfNeeded(Cell landedCell, int landedPos, Monster activeMonster, Runnable onFinished) {
        if (!(landedCell instanceof DoorCell) || !((DoorCell) landedCell).isActivated()) {
            if (onFinished != null) onFinished.run();
            return;
        }

        DoorCell door = (DoorCell) landedCell;
        focusMoveCameraOnPosition(landedPos, 260);

        PauseTransition cameraHold = new PauseTransition(Duration.millis(520));
        cameraHold.setOnFinished(event -> animateDoorUsed(landedPos, door, activeMonster, onFinished));
        cameraHold.play();
    }

    private void animateDoorUsed(int landedPos, DoorCell door, Monster activeMonster, Runnable onFinished) {
        StackPane doorCell = boardRenderer.getCellVisual(landedPos);
        if (doorCell == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        if (activeMonster != null && activeMonster.getRole() == door.getRole()) {
            SoundManager.playDoor(door.getRole());
        }

        Label usedLabel = new Label("Used");
        usedLabel.setMouseTransparent(true);
        usedLabel.setStyle(
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-background-color: rgba(0,0,0,0.78); -fx-padding: 5 10 5 10; -fx-background-radius: 4; " +
                        "-fx-border-color: " + (door.getRole() == Role.SCARER ? "#8a2be2" : "#ffd700") + "; -fx-border-radius: 4;"
        );
        StackPane.setAlignment(usedLabel, Pos.CENTER);
        doorCell.getChildren().removeIf(node -> "door_used_preview".equals(node.getId()));
        usedLabel.setId("door_used_preview");
        usedLabel.setScaleX(0.2);
        usedLabel.setScaleY(0.2);
        doorCell.getChildren().add(usedLabel);

        ScaleTransition openPulse = new ScaleTransition(Duration.millis(360), doorCell);
        openPulse.setFromX(1.0);
        openPulse.setFromY(1.0);
        openPulse.setToX(1.12);
        openPulse.setToY(1.12);
        openPulse.setAutoReverse(true);
        openPulse.setCycleCount(2);

        ScaleTransition labelPop = new ScaleTransition(Duration.millis(260), usedLabel);
        labelPop.setToX(1.0);
        labelPop.setToY(1.0);

        ParallelTransition doorMoment = new ParallelTransition(openPulse, labelPop);
        doorMoment.setOnFinished(event -> {
            PauseTransition holdUsedText = new PauseTransition(Duration.millis(450));
            holdUsedText.setOnFinished(done -> {
                if (onFinished != null) onFinished.run();
            });
            holdUsedText.play();
        });
        doorMoment.play();
    }

    private boolean isComputerTurn() {
        return singlePlayerMode && gameEngine != null && gameEngine.getCurrent() == gameEngine.getOpponent();
    }

    private void finishTurnControls() {
        updateTurnControls();
        scheduleComputerTurnIfNeeded();
    }

    private void updateTurnControls() {
        if (btnRollDice == null || btnPowerup == null || gameEngine == null) return;

        boolean computerTurn = isComputerTurn();
        boolean gameOver = gameEngine.getWinner() != null;
        btnRollDice.setDisable(gameOver || computerTurn);
        btnPowerup.setDisable(gameOver || computerTurn);
    }

    private void scheduleComputerTurnIfNeeded() {
        if (!isComputerTurn() || gameEngine.getWinner() != null) return;

        if (computerTurnDelay != null) {
            computerTurnDelay.stop();
        }

        computerTurnDelay = new PauseTransition(Duration.seconds(1.5));
        computerTurnDelay.setOnFinished(event -> {
            if (isComputerTurn() && gameEngine.getWinner() == null) {
                btnRollDice.setDisable(false);
                btnRollDice.fire();
            }
        });
        computerTurnDelay.play();
    }

    private void makeCurrentPlayerWin() {
        if (gameEngine == null || gameEngine.getWinner() != null) return;

        if (computerTurnDelay != null) {
            computerTurnDelay.stop();
        }

        Monster current = gameEngine.getCurrent();
        current.setPosition(Constants.WINNING_POSITION);
        current.setEnergy(Constants.WINNING_ENERGY);
        refreshBoardNow();
        updateStatusPanelsOnly();
        log("SHORTCUT: " + getDisplayName(current) + " wins by Ctrl+W.");
        checkWinCondition();
    }

    private String getDisplayName(Monster monster) {
        if (gameEngine == null || monster == null) return "";
        return monster == gameEngine.getPlayer() ? playerDisplayName : opponentDisplayName;
    }

    private void focusMoveCameraOnPosition(int position) {
        focusMoveCameraOnPosition(position, 240);
    }

    private void focusMoveCameraOnPosition(int position, int durationMillis) {
        if (boardCameraPane == null || boardRenderer == null) return;

        StackPane focusCell = boardRenderer.getCellVisual(position);
        if (focusCell == null || boardCameraPane.getScene() == null) return;

        Bounds focusBounds = boardCameraPane.sceneToLocal(focusCell.localToScene(focusCell.getBoundsInLocal()));

        double focusX = cellCenterX(focusBounds);
        double focusY = cellCenterY(focusBounds);
        double viewportCenterX = boardCameraPane.getLayoutBounds().getWidth() / 2.0;
        double viewportCenterY = boardCameraPane.getLayoutBounds().getHeight() / 2.0;

        double translateX = (viewportCenterX - focusX) * MOVE_CAMERA_ZOOM;
        double translateY = (viewportCenterY - focusY) * MOVE_CAMERA_ZOOM;

        animateBoardCamera(MOVE_CAMERA_ZOOM, translateX, translateY, durationMillis);
    }

    private void resetMoveCamera() {
        animateBoardCamera(1.0, 0, 0, 320);
    }

    private void animateBoardCamera(double scale, double translateX, double translateY, int durationMillis) {
        if (boardCameraPane == null) return;

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(durationMillis), boardCameraPane);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMillis), boardCameraPane);
        translateTransition.setToX(translateX);
        translateTransition.setToY(translateY);

        ParallelTransition cameraMove = new ParallelTransition(scaleTransition, translateTransition);
        cameraMove.play();
    }

    private double cellCenterX(Bounds bounds) {
        return bounds.getMinX() + bounds.getWidth() / 2.0;
    }

    private double cellCenterY(Bounds bounds) {
        return bounds.getMinY() + bounds.getHeight() / 2.0;
    }

    private void setupSpecialPathDrawing() {
        specialPathRedrawDelay = new PauseTransition(Duration.millis(70));
        specialPathRedrawDelay.setOnFinished(event -> Platform.runLater(this::drawSpecialCellConnectionsFromBoard));

        Platform.runLater(this::requestSpecialPathRedraw);
        boardGrid.widthProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        boardGrid.heightProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        boardGrid.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        boardGrid.localToSceneTransformProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        specialPathPane.widthProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        specialPathPane.heightProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> requestSpecialPathRedraw());
    }

    private void requestSpecialPathRedraw() {
        if (specialPathRedrawDelay == null) {
            Platform.runLater(this::drawSpecialCellConnectionsFromBoard);
            return;
        }

        specialPathRedrawDelay.playFromStart();
    }

    private void drawSpecialCellConnectionsFromBoard() {
        drawSpecialCellConnections(
                getTransportConnectionNodes(ConveyorBelt.class),
                getTransportConnectionNodes(ContaminationSock.class)
        );
    }

    private List<Node> getTransportConnectionNodes(Class<? extends TransportCell> transportType) {
        List<Node> nodes = new ArrayList<>();

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {
            Cell cell = getCellAt(index);
            if (!transportType.isInstance(cell)) continue;

            TransportCell transportCell = (TransportCell) cell;
            int destination = normalizePosition(index + transportCell.getEffect());
            StackPane sourceCell = boardRenderer.getCellVisual(index);
            StackPane destinationCell = boardRenderer.getCellVisual(destination);

            if (sourceCell != null && destinationCell != null) {
                nodes.add(sourceCell);
                nodes.add(destinationCell);
            }
        }

        return nodes;
    }

    private void drawSpecialCellConnections(List<Node> conveyorBelts, List<Node> contaminationSocks) {
        if (specialPathPane == null) return;
        specialPathPane.getChildren().clear();

        drawLinesForType(conveyorBelts, "#00ff66", true);
        drawLinesForType(contaminationSocks, "#ff003c", false);
    }

    private void drawLinesForType(List<Node> cells, String hexColor, boolean conveyorLine) {
        if (cells == null) return;

        for (int i = 0; i < cells.size() - 1; i += 2) {
            Node sourceCell = cells.get(i);
            Node destCell = cells.get(i + 1);

            if (sourceCell == null || destCell == null || sourceCell.getScene() == null || destCell.getScene() == null) {
                continue;
            }

            Bounds sourceBounds = specialPathPane.sceneToLocal(sourceCell.localToScene(sourceCell.getBoundsInLocal()));
            Bounds destBounds = specialPathPane.sceneToLocal(destCell.localToScene(destCell.getBoundsInLocal()));

            double startX = sourceBounds.getMinX() + (sourceBounds.getWidth() / 2.0);
            double startY = sourceBounds.getMinY() + (sourceBounds.getHeight() / 2.0);
            double endX = destBounds.getMinX() + (destBounds.getWidth() / 2.0);
            double endY = destBounds.getMinY() + (destBounds.getHeight() / 2.0);

            Line connectionLine = new Line(startX, startY, endX, endY);
            connectionLine.setMouseTransparent(true);
            connectionLine.setStroke(Color.web(hexColor));
            connectionLine.setStrokeWidth(5.5);
            connectionLine.setOpacity(0.95);
            connectionLine.setBlendMode(BlendMode.SCREEN);
            connectionLine.setEffect(new javafx.scene.effect.DropShadow(14, Color.web(hexColor)));

            if ("#ff003c".equals(hexColor)) {
                connectionLine.getStrokeDashArray().addAll(6.0, 6.0);
            } else {
                connectionLine.getStrokeDashArray().addAll(15.0, 5.0);
            }

            specialPathPane.getChildren().add(connectionLine);
        }
    }

    private String getCellDisplayName(Cell cell) {
        if (cell instanceof DoorCell) return ((DoorCell) cell).getRole() + " Door";
        if (cell instanceof CardCell) return "Card Cell";
        if (cell instanceof ConveyorBelt) return "Conveyor Belt";
        if (cell instanceof ContaminationSock) return "Contamination Sock";
        if (cell instanceof MonsterCell) return "Monster Cell";
        return "Normal Cell";
    }

    private String getConfusionStatus(Monster monster) {
        if (monster.getConfusionTurns() <= 0) return "not confused";
        return monster.getConfusionTurns() + " turns left, acting as " + monster.getRole();
    }

    private void logPowerupDuration(Monster monster) {
        if (monster instanceof Dasher) {
            log("   -> Momentum Rush turns left: " + ((Dasher) monster).getMomentumTurns());
        } else if (monster instanceof MultiTasker) {
            log("   -> Focus Mode turns left: " + ((MultiTasker) monster).getNormalSpeedTurns());
        }
    }

    private Map<Monster, Integer> snapshotStationedMonsterEnergy() {
        Map<Monster, Integer> snapshot = new LinkedHashMap<>();
        for (Monster monster : game.engine.Board.getStationedMonsters()) {
            snapshot.put(monster, monster.getEnergy());
        }
        return snapshot;
    }

    private void logStationedMonsterEnergyChanges(Map<Monster, Integer> beforeEnergy) {
        if (beforeEnergy == null || beforeEnergy.isEmpty()) return;

        boolean anyChange = false;
        for (Map.Entry<Monster, Integer> entry : beforeEnergy.entrySet()) {
            Monster monster = entry.getKey();
            int before = entry.getValue();
            int diff = monster.getEnergy() - before;

            if (diff > 0) {
                log("   -> Stationed " + monster.getName() + " gained " + diff + " Energy.");
                anyChange = true;
            } else if (diff < 0) {
                log("   -> Stationed " + monster.getName() + " lost " + Math.abs(diff) + " Energy.");
                anyChange = true;
            }
        }

        if (anyChange) {
            updateStationedMonstersPanel();
        }
    }

    private void updateDeckStatus() {
        if (lblDeckStatus == null) return;

        int remaining = game.engine.Board.getCards().size();
        int total = game.engine.Board.getOriginalCards().size();
        lblDeckStatus.setText("Cards Remaining: " + remaining + " / " + total);
    }

    private void updateStationedMonstersPanel() {
        if (lblStationedMonsters == null) return;
        lblStationedMonsters.setText(buildStationedMonsterDetails());
    }

    private String buildStationedMonsterDetails() {
        StringBuilder stats = new StringBuilder();
        for (Monster monster : game.engine.Board.getStationedMonsters()) {
            stats.append(monster.getName().toUpperCase())
                    .append("\nRole: ")
                    .append(monster.getRole())
                    .append(" | Type: ")
                    .append(getMonsterType(monster))
                    .append("\nEnergy: ")
                    .append(monster.getEnergy())
                    .append(" | Position: ")
                    .append(monster.getPosition())
                    .append("\nPassive: ")
                    .append(getMonsterPassiveDescription(monster))
                    .append("\nPower-Up: ")
                    .append(getMonsterPowerupDescription(monster))
                    .append("\nStatus: ")
                    .append(getMonsterPowerupStatus(monster))
                    .append("\n\n");
        }

        return stats.length() == 0 ? "No stationed monsters" : stats.toString().trim();
    }

    private static class TurnGuess {
        private final int roll;
        private final int effectiveMove;
        private final int landedPosition;
        private final Cell landedCell;

        private TurnGuess(int roll, int effectiveMove, int landedPosition, Cell landedCell) {
            this.roll = roll;
            this.effectiveMove = effectiveMove;
            this.landedPosition = landedPosition;
            this.landedCell = landedCell;
        }
    }

    // =========================================
    // HELPER METHODS: UI & VISUALS
    // =========================================

    private void updateUI() {
        updateUI(null);
    }

    private void updateUI(Runnable onComplete) {
        if (gameEngine == null) return;

        updateStatusPanelsOnly();
        boardRenderer.updateMonsterPositions(gameEngine.getPlayer(), gameEngine.getOpponent(), gameEngine.getCurrent(), onComplete);
    }

    private void updateUIFollowingMonster(Monster focusMonster, Runnable onComplete) {
        if (gameEngine == null) return;

        updateStatusPanelsOnly();
        boolean followPlayer = focusMonster == gameEngine.getPlayer();
        boardRenderer.updateMonsterPositions(
                gameEngine.getPlayer(),
                gameEngine.getOpponent(),
                gameEngine.getCurrent(),
                followPlayer,
                this::focusMoveCameraOnPosition,
                onComplete
        );
    }

    private void updateStatusPanelsOnly() {
        Monster player = gameEngine.getPlayer();
        Monster opponent = gameEngine.getOpponent();
        Monster current = gameEngine.getCurrent();

        if (singlePlayerMode) {
            Monster displayedTurn = singlePlayerTurnLabelOverride != null ? singlePlayerTurnLabelOverride : current;
            if (displayedTurn == player) {
                lblTurnInfo.setText("Your Turn");
                lblTurnInfo.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 32px; -fx-font-weight: bold;");
            } else {
                lblTurnInfo.setText("Computer Turn");
                lblTurnInfo.setStyle("-fx-text-fill: #ff003c; -fx-font-size: 32px; -fx-font-weight: bold;");
            }
        } else if (current == player) {
            lblTurnInfo.setText("TURN: " + player.getName().toUpperCase() + " (" + playerDisplayName.toUpperCase() + ")");
            lblTurnInfo.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 32px; -fx-font-weight: bold;");
        } else {
            lblTurnInfo.setText("TURN: " + opponent.getName().toUpperCase() + " (" + opponentDisplayName.toUpperCase() + ")");
            lblTurnInfo.setStyle("-fx-text-fill: #ff003c; -fx-font-size: 32px; -fx-font-weight: bold;");
        }

        updateStatPanel(player, lblPlayerStats, "--- " + playerDisplayName.toUpperCase() + " ---");
        updateStatPanel(opponent, lblOpponentStats, "--- " + opponentDisplayName.toUpperCase() + " ---");
        updateDeckStatus();
        updateStationedMonstersPanel();
    }

    private void playDiceFaceAnimation(int roll, Runnable onFinished) {
        AnimationManager.animateDiceFaceRoll(diceFaceLabel, roll, onFinished);
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
                            "-fx-background-radius: 0; " + // <--- This removes the rounded background corners
                            "-fx-border-radius: 0; " +     // <--- This ensures the border is a sharp square
                            "-fx-border-color: transparent; " + // Keeps it clean within the square
                            "-fx-cursor: hand;"
            );

            // Visual Hover state changes
            btnDismiss.setOnMouseEntered(ev -> btnDismiss.setStyle(
                    "-fx-background-color: #ff3366; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-font-size: 13px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"
            ));

            btnDismiss.setOnMouseExited(ev -> btnDismiss.setStyle(
                    "-fx-background-color: #ff003c; -fx-text-fill: white; -fx-font-weight: bold; " +
                            "-fx-font-size: 13px; -fx-background-radius: 0; -fx-border-radius: 0; -fx-cursor: hand;"
            ));

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
            final boolean[] closing = {false};
            Runnable closePopup = () -> {
                if (closing[0]) return;
                closing[0] = true;
                btnDismiss.setDisable(true);
                javafx.animation.ScaleTransition popOut = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(150), squareCard);
                popOut.setToX(0);
                popOut.setToY(0);
                popOut.setOnFinished(finishEvent -> {
                    rootPane.getChildren().remove(overlay); // Clears overlay smoothly from screen layout hierarchy
                });
                popOut.play();
            };

            btnDismiss.setOnAction(clickEvent -> {
                clickEvent.consume();
                closePopup.run();
            });

            overlay.setOnMouseClicked(mouseEvent -> {
                mouseEvent.consume();
                closePopup.run();
            });
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
            log("GAME OVER! " + getDisplayName(winner) + " WINS!");
            btnRollDice.setDisable(true);
            btnPowerup.setDisable(true);
            stopAudio();

            Platform.runLater(() -> {
                Stage currentStage = (Stage) boardGrid.getScene().getWindow();
                WinWindow.display(winner, gameEngine.getPlayer(), gameEngine.getOpponent(), currentStage,
                        playerDisplayName, opponentDisplayName, singlePlayerMode);
            });
        }
    }

    private void showStationedMonstersPopup() {
        if (rootPane == null) return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");

        VBox panel = new VBox(14);
        panel.setAlignment(javafx.geometry.Pos.CENTER);
        panel.setMaxWidth(520);
        panel.setMaxHeight(560);
        panel.setStyle(
                "-fx-background-color: rgba(8, 18, 28, 0.96); " +
                        "-fx-border-color: #00d4ff; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 22;"
        );
        panel.setEffect(new javafx.scene.effect.DropShadow(28, javafx.scene.paint.Color.web("#00d4ff")));

        Label title = new Label("STATIONED MONSTERS");
        title.setStyle("-fx-text-fill: #00d4ff; -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox monsterList = new VBox(12);
        monsterList.setPadding(new Insets(12));
        monsterList.setFillWidth(true);

        for (Monster monster : game.engine.Board.getStationedMonsters()) {
            monsterList.getChildren().add(createStationedMonsterCard(monster));
        }

        if (monsterList.getChildren().isEmpty()) {
            Label empty = new Label("No stationed monsters");
            empty.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            empty.setAlignment(Pos.CENTER);
            monsterList.getChildren().add(empty);
        }

        ScrollPane scrollPane = new ScrollPane(monsterList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(390);
        scrollPane.setMaxWidth(470);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: rgba(0,212,255,0.35);");

        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(150);
        closeButton.setPrefHeight(36);
        closeButton.setStyle(
                "-fx-background-color: #00d4ff; " +
                        "-fx-text-fill: #061018; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
        );

        closeButton.setOnAction(e -> rootPane.getChildren().remove(overlay));
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                rootPane.getChildren().remove(overlay);
            }
            e.consume();
        });

        panel.getChildren().addAll(title, scrollPane, closeButton);
        overlay.getChildren().add(panel);
        rootPane.getChildren().add(overlay);
    }

    private VBox createStationedMonsterCard(Monster monster) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setMaxWidth(430);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); " +
                        "-fx-border-color: rgba(0,212,255,0.35); " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-radius: 6;"
        );

        Label name = new Label(monster.getName());
        name.setWrapText(true);
        name.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label role = createStationedBadge(monster.getRole().toString(), monster.getRole() == Role.SCARER ? "#8a2be2" : "#ffd700", monster.getRole() == Role.SCARER ? "white" : "#101018");
        Label type = createStationedBadge(getMonsterType(monster), "#00d4ff", "#061018");
        Label energy = createStationedBadge("Energy " + monster.getEnergy(), "#263746", "white");
        Label position = createStationedBadge("Cell " + monster.getPosition(), "#263746", "white");
        meta.getChildren().addAll(role, type, energy, position);

        Label passive = createStationedLine("Passive", getMonsterPassiveDescription(monster));
        Label powerup = createStationedLine("Power-Up", getMonsterPowerupDescription(monster));
        Label status = createStationedLine("Status", getMonsterPowerupStatus(monster));

        card.getChildren().addAll(name, meta, passive, powerup, status);
        return card;
    }

    private Label createStationedBadge(String text, String background, String textColor) {
        Label badge = new Label(text);
        badge.setStyle(
                "-fx-background-color: " + background + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; " +
                        "-fx-padding: 3 7 3 7; -fx-background-radius: 10;"
        );
        return badge;
    }

    private Label createStationedLine(String label, String value) {
        Label line = new Label(label + ": " + value);
        line.setWrapText(true);
        line.setMaxWidth(400);
        line.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-line-spacing: 2px;");
        return line;
    }

    private String getMonsterType(Monster m) {
        return m.getClass().getSimpleName();
    }

    private String getMonsterPassiveDescription(Monster m) {
        if (m instanceof Dasher) return "Lightning Movement: dice movement is doubled.";
        if (m instanceof Dynamo) return "Energy Amplification: gains and losses are doubled.";
        if (m instanceof MultiTasker) return "Movement-Energy: movement halved, energy changes gain +200.";
        if (m instanceof Schemer) return "Energy Manipulation: energy changes gain a +10 bonus.";
        return "No passive trait.";
    }

    private String getMonsterPowerupName(Monster m) {
        if (m instanceof Dasher) return "Momentum Rush";
        if (m instanceof Dynamo) return "Energy Freeze";
        if (m instanceof MultiTasker) return "Focus Mode";
        if (m instanceof Schemer) return "Chain Attack";
        return "Power-Up";
    }

    private String getMonsterPowerupDescription(Monster m) {
        if (m instanceof Dasher) return "Momentum Rush: 3x movement for 3 turns.";
        if (m instanceof Dynamo) return "Energy Freeze: opponent skips their next turn.";
        if (m instanceof MultiTasker) return "Focus Mode: normal movement for 2 turns.";
        if (m instanceof Schemer) return "Chain Attack: steals 10 energy from every other monster.";
        return "No power-up description.";
    }

    private String getMonsterPowerupStatus(Monster m) {
        if (m instanceof Dasher) {
            int turns = ((Dasher) m).getMomentumTurns();
            return turns > 0 ? "Momentum Rush active: " + turns + " turns left." : "Momentum Rush inactive.";
        }
        if (m instanceof Dynamo) {
            return "Energy Freeze ready: freezes opponent for 1 turn.";
        }
        if (m instanceof MultiTasker) {
            int turns = ((MultiTasker) m).getNormalSpeedTurns();
            return turns > 0 ? "Focus Mode active: " + turns + " turns left." : "Focus Mode inactive.";
        }
        if (m instanceof Schemer) {
            return "Chain Attack ready: shield does not block it.";
        }
        return "No active status.";
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
        displayLabel.setWrapText(true);
        displayLabel.setMaxWidth(245);
        displayLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-line-spacing: 2px;");

        String originalRole = m.getOriginalRole().toString();
        String currentRole = m.getRole().toString();

        String roleDisplay = originalRole;
        if (!originalRole.equals(currentRole)) {
            roleDisplay = originalRole + " (Confused as " + currentRole + ")";
        }

        String stats = String.format(
                "%s\nName: %s\nBase Role: %s\nCurrent Role: %s\nType: %s\nEnergy: %d\nPosition: %d\nEffects: %s\nPassive: %s\nPower-Up: %s\nStatus: %s",
                title, m.getName(), m.getOriginalRole(), roleDisplay, getMonsterType(m), m.getEnergy(), m.getPosition(),
                getActiveEffects(m), getMonsterPassiveDescription(m), getMonsterPowerupDescription(m), getMonsterPowerupStatus(m)
        );

        displayLabel.setText(stats);
    }

    public void stopAudio() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
            backgroundMusicPlayer = null;
        }
    }

    private static Image loadGameImage(String path) {
        try {
            URL url = GameController.class.getResource(path);
            return url == null ? null : new Image(url.toExternalForm());
        } catch (Exception e) {
            return null;
        }
    }
}
