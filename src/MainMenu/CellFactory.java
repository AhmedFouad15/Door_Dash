package MainMenu;

import game.engine.cells.*;
import game.engine.Role;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.Map;

public class CellFactory {

    // --- STEP 1: PRE-LOAD IMAGES (STATIC CACHING) ---
    // This loads the images once when the game starts, saving your RAM.
    private static final Image CARD_IMAGE = loadImage("/MainMenu/assets/images/card_icon.png");
    private static final Image CONVEYOR_IMAGE = loadImage("/MainMenu/assets/images/conveyor_icon.png");
    private static final Image SOCK_IMAGE = loadImage("/MainMenu/assets/images/sock_icon.png");
    private static final Image DOOR_SCARER_IMAGE = loadImage("/MainMenu/assets/images/door_scarer.png");
    private static final Image DOOR_LAUGHER_IMAGE = loadImage("/MainMenu/assets/images/door_laugher.png");
    private static final Image RANDALL_IMAGE = loadImage("/MainMenu/assets/images/randall.png");
    private static final Image CELIA_IMAGE   = loadImage("/MainMenu/assets/images/celia.png");
    private static final Image HENRY_IMAGE   = loadImage("/MainMenu/assets/images/Henry.png");
    private static final Image ROZ_IMAGE     = loadImage("/MainMenu/assets/images/roz.png");
    private static final Image FUNGUS_IMAGE  = loadImage("/MainMenu/assets/images/fungus.png");
    private static final Image YETI_IMAGE    = loadImage("/MainMenu/assets/images/yeti.png");

    private static final Image SULLEY_IMAGE = loadImage("/MainMenu/assets/images/scarer_token.png");
    private static final Image MIKE_IMAGE = loadImage("/MainMenu/assets/images/laugher_token.png");

    private static final Map<String, Image> MONSTER_IMAGES = Map.of(
            "James P. Sullivan", SULLEY_IMAGE,
            "Mike Wazowski", MIKE_IMAGE,
            "Randall Boggs", RANDALL_IMAGE,
            "Celia Mae", CELIA_IMAGE,
            "Henry J. Waternoose", HENRY_IMAGE,
            "Roz", ROZ_IMAGE,
            "Fungus", FUNGUS_IMAGE,
            "Yeti", YETI_IMAGE
    );

    public static StackPane createVisualCell(int index, Cell cellBackend) {
        StackPane visualCell = new StackPane();
        visualCell.setPrefSize(70, 70);

        String bgColor = "rgba(255, 255, 0, 0.2)";
        String borderColor = "rgba(255, 255, 0, 0.5)";
        Color glowColor = Color.YELLOW;

        // Use Image object instead of path string
        Image cellIcon = null;

        if (cellBackend instanceof MonsterCell) {
            bgColor = "rgba(0, 0, 255, 0.3)";
            borderColor = "#0000ff";
            glowColor = Color.BLUE;

            MonsterCell monsterCell = (MonsterCell) cellBackend;
            cellIcon = MONSTER_IMAGES.get(monsterCell.getCellMonster().getName());
        }
        else if (cellBackend instanceof CardCell) {
            bgColor = "rgba(255, 0, 0, 0.3)";
            borderColor = "#ff0000";
            glowColor = Color.RED;
            cellIcon = CARD_IMAGE; // Use cached image
        }
        else if (cellBackend instanceof ConveyorBelt) {
            bgColor = "rgba(0, 255, 0, 0.3)";
            borderColor = "#00ff00";
            glowColor = Color.GREEN;
            cellIcon = CONVEYOR_IMAGE; // Use cached image
        }
        else if (cellBackend instanceof ContaminationSock) {
            bgColor = "rgba(255, 165, 0, 0.3)";
            borderColor = "#ffa500";
            glowColor = Color.ORANGE;
            cellIcon = SOCK_IMAGE; // Use cached image
        }
        else if (cellBackend instanceof DoorCell) {
            DoorCell door = (DoorCell) cellBackend;
            if (door.isActivated()) {
                bgColor = "rgba(50, 50, 50, 0.4)";
                borderColor = "gray";
                glowColor = Color.GRAY;
                cellIcon = (door.getRole() == Role.SCARER) ? DOOR_SCARER_IMAGE : DOOR_LAUGHER_IMAGE;
            } else {
                String color = (door.getRole() == Role.SCARER) ? "#8a2be2" : "#ffd700";
                bgColor = (door.getRole() == Role.SCARER) ? "rgba(138, 43, 226, 0.3)" : "rgba(255, 215, 0, 0.3)";
                borderColor = color;
                glowColor = Color.web(color);
                cellIcon = (door.getRole() == Role.SCARER) ? DOOR_SCARER_IMAGE : DOOR_LAUGHER_IMAGE;
            }
        }

        visualCell.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor +
                "; -fx-border-width: 2; -fx-border-radius: 8;");

        // --- STEP 2: USE THE CACHED IMAGE ---
        if (cellIcon != null) {
            ImageView iconView = new ImageView(cellIcon);
            iconView.setFitWidth(35);
            iconView.setFitHeight(35);
            if (cellBackend instanceof DoorCell && ((DoorCell) cellBackend).isActivated()) {
                iconView.setOpacity(0.45);
                iconView.setRotate(-10);
            }
            visualCell.getChildren().add(iconView);

            if (cellBackend instanceof ConveyorBelt) {
                applyConveyorAnimation(iconView);
            }
        }

        if (cellBackend instanceof DoorCell) {
            DoorCell door = (DoorCell) cellBackend;
            Label doorInfo = new Label((door.getRole() == Role.SCARER ? "S" : "L") + "\n" + door.getEnergy());
            doorInfo.setTextAlignment(TextAlignment.CENTER);
            doorInfo.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: rgba(0,0,0,0.55); -fx-padding: 2 4 2 4; -fx-background-radius: 4;"
            );
            StackPane.setAlignment(doorInfo, Pos.BOTTOM_RIGHT);
            visualCell.getChildren().add(doorInfo);

            if (door.isActivated()) {
                Label exhausted = new Label("Used");
                exhausted.setStyle(
                        "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                                "-fx-background-color: rgba(0,0,0,0.72); -fx-padding: 3 6 3 6; -fx-background-radius: 4;"
                );
                StackPane.setAlignment(exhausted, Pos.CENTER);
                visualCell.getChildren().add(exhausted);
            }
        }

        if (cellBackend instanceof MonsterCell) {
            MonsterCell monsterCell = (MonsterCell) cellBackend;
            Label monsterName = new Label(shortMonsterName(monsterCell.getCellMonster().getName()));
            monsterName.setTextAlignment(TextAlignment.CENTER);
            monsterName.setWrapText(true);
            monsterName.setMaxWidth(62);
            monsterName.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                            "-fx-background-color: rgba(0,0,0,0.76); -fx-padding: 2 4 2 4; -fx-background-radius: 4;"
            );
            StackPane.setAlignment(monsterName, Pos.BOTTOM_CENTER);
            visualCell.getChildren().add(monsterName);
        }

        if (cellBackend instanceof TransportCell) {
            TransportCell transportCell = (TransportCell) cellBackend;
            Label effectLabel = new Label((transportCell.getEffect() > 0 ? "+" : "") + transportCell.getEffect());
            effectLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");
            StackPane.setAlignment(effectLabel, Pos.BOTTOM_RIGHT);
            visualCell.getChildren().add(effectLabel);
        }

        Label numberLabel = new Label(String.valueOf(index));
        numberLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6); -fx-font-weight: bold;");
        StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
        visualCell.getChildren().add(numberLabel);

        applyHoverEffects(visualCell, glowColor);

        if (cellBackend instanceof CardCell) {
            applyCardPulse(visualCell);
        }

        return visualCell;
    }

    // Helper method to load images safely
    private static Image loadImage(String path) {
        try {
            return new Image(CellFactory.class.getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Could not load image: " + path);
            return null;
        }
    }

    private static String shortMonsterName(String name) {
        if (name == null || name.isBlank()) return "Monster";
        String[] parts = name.trim().split("\\s+");
        return parts[0].length() > 8 ? parts[0].substring(0, 8) : parts[0];
    }

    private static void applyHoverEffects(StackPane cell, Color glowColor) {
        DropShadow shadow = new DropShadow(10, Color.BLACK);
        cell.setEffect(shadow);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), cell);
        scaleIn.setToX(1.1); scaleIn.setToY(1.1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), cell);
        scaleOut.setToX(1.0); scaleOut.setToY(1.0);

        cell.setOnMouseEntered(e -> {
            cell.toFront();
            scaleIn.playFromStart();
            shadow.setColor(glowColor);
            shadow.setRadius(20);
        });
        cell.setOnMouseExited(e -> {
            scaleOut.playFromStart();
            shadow.setColor(Color.BLACK);
            shadow.setRadius(10);
        });
    }

    private static void applyConveyorAnimation(ImageView icon) {
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.seconds(1), icon);
        tt.setFromX(-5); tt.setToX(5);
        tt.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tt.setAutoReverse(true);
        tt.play();
    }

    private static void applyCardPulse(StackPane cell) {
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.seconds(2), cell);
        ft.setFromValue(0.6); ft.setToValue(1.0);
        ft.setAutoReverse(true);
        ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
        ft.play();
    }
}
