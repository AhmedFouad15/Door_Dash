package MainMenu;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;

public class AnimationManager {

    /**
     * 1. DICE ANIMATION
     * Shakes and scales the button, then runs the backend logic when finished.
     */
    public static void animateDiceRoll(Node diceNode, Runnable onAnimationFinished) {
        // Quick spin
        RotateTransition rotate = new RotateTransition(Duration.millis(300), diceNode);
        rotate.setByAngle(360);
        rotate.setCycleCount(1);

        // Quick "pop" scale effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), diceNode);
        scale.setByX(0.1);
        scale.setByY(0.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        // Run them at the same time
        ParallelTransition pt = new ParallelTransition(rotate, scale);

        // CRITICAL: Tell the game to actually make the move AFTER the animation ends
        pt.setOnFinished(e -> onAnimationFinished.run());
        pt.play();
    }

    /**
     * 2. ENERGY LOSS/GAIN FLASH
     * Creates floating text that rises from a cell and fades away.
     */
    public static void animateFloatingText(StackPane cell, String text, boolean isPositive) {
        Label floatingLabel = new Label(text);

        // Green for positive (gained energy), Red for negative (lost energy/damage)
        String color = isPositive ? "#32cd32" : "#ff003c";
        floatingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        // Add a glow to the text
        DropShadow glow = new DropShadow(10, Color.web(color));
        floatingLabel.setEffect(glow);

        // Add it to the cell
        cell.getChildren().add(floatingLabel);

        // Move it UP by 60 pixels
        TranslateTransition moveUp = new TranslateTransition(Duration.seconds(1.2), floatingLabel);
        moveUp.setByY(-60);

        // Fade it out at the same time
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.2), floatingLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(moveUp, fadeOut);

        // Clean up the label from memory after it becomes invisible
        pt.setOnFinished(e -> cell.getChildren().remove(floatingLabel));
        pt.play();
    }

    /**
     * 3. CARD REVEAL POPUP
     * Scales a node in from 0 to 1 with a slight bounce.
     */
    public static void animateCardReveal(Node cardVisual) {
        cardVisual.setScaleX(0);
        cardVisual.setScaleY(0);

        ScaleTransition popIn = new ScaleTransition(Duration.millis(400), cardVisual);
        popIn.setToX(1.0);
        popIn.setToY(1.0);
        popIn.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.5)); // Adds a "bounce" effect
        popIn.play();
    }

    /**
     * 4. SMOOTH MONSTER MOVEMENT
     * Calculates the distance between two grid cells and slides the monster.
     */
    public static void animateMonsterMove(Node monsterVisual, StackPane oldCell, StackPane newCell, Runnable onFinished) {
        // Bring monster to the front so it doesn't slide "under" other cells
        monsterVisual.toFront();

        // Calculate the exact pixel distance between the old cell and the new cell
        double deltaX = newCell.getLayoutX() - oldCell.getLayoutX();
        double deltaY = newCell.getLayoutY() - oldCell.getLayoutY();

        // Create the sliding animation (Takes 0.5 seconds)
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), monsterVisual);
        slide.setByX(deltaX);
        slide.setByY(deltaY);
        slide.setInterpolator(Interpolator.EASE_BOTH); // Makes it speed up and slow down smoothly

        // What to do when it reaches the destination
        slide.setOnFinished(e -> {
            // Reset the translation values back to 0
            monsterVisual.setTranslateX(0);
            monsterVisual.setTranslateY(0);
            // Run the "snap to new cell" logic
            onFinished.run();
        });

        slide.play();
    }

    /**
     * ENERGY LOSS: Rapid Shake + Red Flash
     */
    public static void animateDamageShake(Node node) {
        // The Shake
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setByX(10); // Move 10px right
        shake.setCycleCount(6); // Do it 6 times
        shake.setAutoReverse(true); // Bounce back and forth

        // The Red Flash (Using CSS transition via code)
        String originalStyle = node.getStyle();
        node.setStyle(originalStyle + "-fx-effect: dropshadow(gaussian, #ff003c, 30, 0.8, 0, 0);");

        shake.setOnFinished(e -> {
            node.setTranslateX(0); // Reset position safely
            node.setStyle(originalStyle); // Remove red flash
        });

        shake.play();
    }

    /**
     * SHIELD BLOCK: Blue Pulse
     */
    public static void animateShieldPulse(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(200), node);
        pulse.setToX(1.3);
        pulse.setToY(1.3);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    /**
     * CARD DRAW POPUP: Glassmorphism Screen Overlay
     */
    public static void showCardPopup(StackPane rootPane, String cardName, String cardEffect) {
        // 1. Create the Dark Overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        // 2. Create the Card Visual
        javafx.scene.layout.VBox cardVisual = new javafx.scene.layout.VBox(20);
        cardVisual.setAlignment(javafx.geometry.Pos.CENTER);
        cardVisual.setMaxSize(300, 450);
        cardVisual.setStyle(
                "-fx-background-color: rgba(138, 43, 226, 0.2); " +
                        "-fx-border-color: #ff00ff; -fx-border-width: 3; -fx-border-radius: 15; " +
                        "-fx-background-radius: 15; -fx-padding: 30;"
        );
        cardVisual.setEffect(new DropShadow(30, Color.MAGENTA));

        // 3. Card Content
        Label title = new Label("CARD DRAWN!");
        title.setStyle("-fx-text-fill: #ff00ff; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label name = new Label(cardName);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label effect = new Label(cardEffect);
        effect.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 16px;");
        effect.setWrapText(true);
        effect.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Click anywhere to dismiss
        Label hint = new Label("(Click anywhere to continue)");
        hint.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");

        cardVisual.getChildren().addAll(title, name, effect, hint);
        overlay.getChildren().add(cardVisual);

        // 4. Animation In
        rootPane.getChildren().add(overlay);
        cardVisual.setScaleX(0);
        cardVisual.setScaleY(0);

        ScaleTransition popIn = new ScaleTransition(Duration.millis(400), cardVisual);
        popIn.setToX(1.0); popIn.setToY(1.0);
        popIn.setInterpolator(javafx.animation.Interpolator.SPLINE(0.25, 0.1, 0.25, 1.5));
        popIn.play();

        // 5. Dismiss Logic
        overlay.setOnMouseClicked(e -> {
            ScaleTransition popOut = new ScaleTransition(Duration.millis(200), cardVisual);
            popOut.setToX(0); popOut.setToY(0);
            popOut.setOnFinished(event -> rootPane.getChildren().remove(overlay));
            popOut.play();
        });
    }
}