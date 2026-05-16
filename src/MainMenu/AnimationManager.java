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
        // Create the sliding animation (Decreased by 66% for much faster movement)
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), monsterVisual);
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
    /**
     * CARD DRAW POPUP: Interactive Flip (Click to Reveal, Click to Dismiss)
     */
    public static void showCardPopup(StackPane rootPane, String cardName, String cardEffect) {
        // 1. Create the Dark Overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        // 2. Create the Card Container
        javafx.scene.layout.VBox cardVisual = new javafx.scene.layout.VBox(20);
        cardVisual.setAlignment(javafx.geometry.Pos.CENTER);
        cardVisual.setMaxSize(300, 450);
        cardVisual.setStyle(
                "-fx-background-color: #2c3e50; " +
                        "-fx-border-color: #ff00ff; -fx-border-width: 5; -fx-border-radius: 15; " +
                        "-fx-background-radius: 15; -fx-padding: 30;"
        );
        cardVisual.setEffect(new DropShadow(30, Color.MAGENTA));

        // 3. Create the "Back" of the card (Visible first)
        Label backLabel = new Label("?");
        backLabel.setStyle("-fx-font-size: 100px; -fx-text-fill: white; -fx-font-weight: bold;");
        Label clickHint = new Label("(Click to Reveal)");
        clickHint.setStyle("-fx-text-fill: gray; -fx-font-size: 14px;");

        javafx.scene.layout.VBox backContent = new javafx.scene.layout.VBox(10, backLabel, clickHint);
        backContent.setAlignment(javafx.geometry.Pos.CENTER);
        cardVisual.getChildren().add(backContent);

        // 4. Create the "Front" Content (Hidden initially)
        javafx.scene.layout.VBox frontContent = new javafx.scene.layout.VBox(20);
        frontContent.setAlignment(javafx.geometry.Pos.CENTER);

        Label title = new Label("CARD DRAWN!");
        title.setStyle("-fx-text-fill: #ff00ff; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label name = new Label(cardName);
        name.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 26px; -fx-font-weight: bold;");

        Label effect = new Label(cardEffect);
        effect.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        effect.setWrapText(true);
        effect.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label closeHint = new Label("(Click to Continue)");
        closeHint.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");

        frontContent.getChildren().addAll(title, name, effect, closeHint);

        // 5. Add to Screen
        overlay.getChildren().add(cardVisual);
        rootPane.getChildren().add(overlay);

        // 6. Interactive Flip State Machine
        final boolean[] isRevealed = {false};

        cardVisual.setOnMouseClicked(e -> {
            if (!isRevealed[0]) {
                // --- FIRST CLICK: REVEAL ---
                isRevealed[0] = true;

                // Shrink card horizontally to 0 (gives the illusion of turning sideways)
                ScaleTransition flipToEdge = new ScaleTransition(Duration.millis(250), cardVisual);
                flipToEdge.setToX(0);

                flipToEdge.setOnFinished(ev -> {
                    // Swap the "?" for the actual card text while it's invisible
                    cardVisual.getChildren().clear();
                    cardVisual.getChildren().add(frontContent);

                    // Expand card back to full width
                    ScaleTransition flipToFront = new ScaleTransition(Duration.millis(250), cardVisual);
                    flipToFront.setToX(1);
                    flipToFront.play();
                });

                flipToEdge.play();
            } else {
                // --- SECOND CLICK: DISMISS ---
                ScaleTransition popOut = new ScaleTransition(Duration.millis(200), cardVisual);
                popOut.setToX(0);
                popOut.setToY(0);
                popOut.setOnFinished(event -> rootPane.getChildren().remove(overlay));
                popOut.play();
            }
        });

        // Block clicks on the background overlay so they *must* click the card
        overlay.setOnMouseClicked(e -> e.consume());
    }
}