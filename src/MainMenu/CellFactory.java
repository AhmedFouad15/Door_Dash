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
import javafx.util.Duration;

public class CellFactory {

    public static StackPane createVisualCell(int index, Cell cellBackend) {
        StackPane visualCell = new StackPane();
        visualCell.setPrefSize(70, 70);

        // Default: Yellow for Normal Cells as requested
        String bgColor = "rgba(255, 255, 0, 0.2)";
        String borderColor = "rgba(255, 255, 0, 0.5)";
        Color glowColor = Color.YELLOW;
        String iconPath = null;

        // --- IDENTIFY CELL TYPE & APPLY REQUESTED COLORS ---
        if (cellBackend instanceof MonsterCell) {
            // Monster Cell: Blue background
            bgColor = "rgba(0, 0, 255, 0.3)";
            borderColor = "#0000ff";
            glowColor = Color.BLUE;
        }
        else if (cellBackend instanceof CardCell) {
            // Card Cell: Red background
            bgColor = "rgba(255, 0, 0, 0.3)";
            borderColor = "#ff0000";
            glowColor = Color.RED;
            iconPath = "/MainMenu/assets/images/card_icon.png";
        }
        else if (cellBackend instanceof ConveyorBelt) {
            // Conveyor: Green background
            bgColor = "rgba(0, 255, 0, 0.3)";
            borderColor = "#00ff00";
            glowColor = Color.GREEN;
            iconPath = "/MainMenu/assets/images/conveyor_icon.png";
        }
        else if (cellBackend instanceof ContaminationSock) {
            // Contamination Sock: Orange background
            bgColor = "rgba(255, 165, 0, 0.3)";
            borderColor = "#ffa500";
            glowColor = Color.ORANGE;
            iconPath = "/MainMenu/assets/images/sock_icon.png";
        }
        else if (cellBackend instanceof DoorCell) {
            // Maintaining Door logic from Milestone 3 requirements
            DoorCell door = (DoorCell) cellBackend;
            if (door.isActivated()) {
                bgColor = "rgba(50, 50, 50, 0.4)";
                borderColor = "gray";
            } else {
                String color = (door.getRole() == Role.SCARER) ? "#8a2be2" : "#ffd700";
                bgColor = (door.getRole() == Role.SCARER) ? "rgba(138, 43, 226, 0.3)" : "rgba(255, 215, 0, 0.3)";
                borderColor = color;
                glowColor = Color.web(color);
                iconPath = (door.getRole() == Role.SCARER) ? "/MainMenu/assets/images/door_scarer.png" : "/MainMenu/assets/images/door_laugher.png";
            }
        }

        visualCell.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor +
                "; -fx-border-width: 2; -fx-border-radius: 8;");

        // --- ADD ICON ---
        if (iconPath != null) {
            try {
                ImageView iconView = new ImageView(new Image(CellFactory.class.getResourceAsStream(iconPath)));
                iconView.setFitWidth(35);
                iconView.setFitHeight(35);
                visualCell.getChildren().add(iconView);

                if (cellBackend instanceof ConveyorBelt) {
                    applyConveyorAnimation(iconView);
                }
            } catch (Exception e) {
                // Silently skip if asset is missing
            }
        }

        // --- ADD CELL NUMBER ---
        Label numberLabel = new Label(String.valueOf(index));
        numberLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6); -fx-font-weight: bold;");
        StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
        visualCell.getChildren().add(numberLabel);

        // --- HOVER EFFECTS & AMBIENT PULSE ---
        applyHoverEffects(visualCell, glowColor);

        if (cellBackend instanceof CardCell) {
            applyCardPulse(visualCell);
        }

        return visualCell;
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