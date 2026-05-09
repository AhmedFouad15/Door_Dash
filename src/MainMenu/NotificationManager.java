package MainMenu;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class NotificationManager {

    /**
     * The Master Method for Floating Combat Text
     */
    public static void showFloatingText(StackPane targetCell, String text, Color color, int yOffset) {
        if (targetCell == null) return;

        Label floatingLabel = new Label(text);
        floatingLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        floatingLabel.setTextFill(color);

        // Cyber-Glow Effect
        DropShadow glow = new DropShadow(15, color);
        glow.setSpread(0.5);
        floatingLabel.setEffect(glow);

        // Add to the cell (it will overlay whatever is there)
        targetCell.getChildren().add(floatingLabel);

        // 1. Move Upwards
        TranslateTransition moveUp = new TranslateTransition(Duration.seconds(1.5), floatingLabel);
        moveUp.setByY(yOffset); // e.g., -60 moves it up

        // 2. Fade Out
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), floatingLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Run both at the same time
        ParallelTransition pt = new ParallelTransition(moveUp, fadeOut);

        // CLEANUP: Remove from memory when done!
        pt.setOnFinished(e -> targetCell.getChildren().remove(floatingLabel));
        pt.play();
    }

    // --- QUICK HELPERS FOR GAME EVENTS ---

    public static void showDamage(StackPane targetCell, int amount) {
        showFloatingText(targetCell, "-" + amount + " ENERGY", Color.web("#ff003c"), -70);
    }

    public static void showHeal(StackPane targetCell, int amount) {
        showFloatingText(targetCell, "+" + amount + " ENERGY", Color.web("#32cd32"), -70);
    }

    public static void showShieldBlock(StackPane targetCell) {
        showFloatingText(targetCell, "BLOCKED!", Color.AQUA, -50);
    }

    public static void showRoleSwap(StackPane targetCell) {
        showFloatingText(targetCell, "ROLE SWAPPED!", Color.MAGENTA, -80);
    }
}