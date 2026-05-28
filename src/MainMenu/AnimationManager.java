package MainMenu;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
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
        if (onAnimationFinished != null) onAnimationFinished.run();
    }

    public static void animateDiceFaceRoll(Label diceLabel, int finalRoll, Runnable onFinished) {
        if (diceLabel == null) {
            if (onFinished != null) onFinished.run();
            return;
        }

        diceLabel.setVisible(true);
        diceLabel.setText("-");
        diceLabel.setScaleX(1.0);
        diceLabel.setScaleY(1.0);
        diceLabel.setRotate(0);
        diceLabel.setEffect(new DropShadow(18, Color.web("#00d4ff")));

        Timeline rolling = new Timeline();
        int frames = 20;
        for (int i = 0; i <= frames; i++) {
            final int face = (i % 6) + 1;
            KeyFrame frame = new KeyFrame(Duration.millis(i * 80), event -> {
                diceLabel.setText(String.valueOf(face));
                diceLabel.setRotate((face % 2 == 0) ? 8 : -8);
                diceLabel.setScaleX(1.05);
                diceLabel.setScaleY(1.05);
            });
            rolling.getKeyFrames().add(frame);
        }

        rolling.setOnFinished(event -> {
            diceLabel.setText(String.valueOf(finalRoll));
            diceLabel.setRotate(0);
            diceLabel.setScaleX(1.0);
            diceLabel.setScaleY(1.0);

            DropShadow finalGlow = new DropShadow();
            finalGlow.setBlurType(BlurType.GAUSSIAN);
            finalGlow.setColor(Color.GOLD);
            finalGlow.setRadius(28);
            finalGlow.setSpread(0.45);
            diceLabel.setEffect(finalGlow);

            ScaleTransition pulse = new ScaleTransition(Duration.millis(225), diceLabel);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.22);
            pulse.setToY(1.22);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.setOnFinished(finish -> {
                diceLabel.setScaleX(1.0);
                diceLabel.setScaleY(1.0);
                diceLabel.setEffect(null);
                if (onFinished != null) onFinished.run();
            });
            pulse.play();
        });

        rolling.play();
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

        TranslateTransition slide = new TranslateTransition(Duration.millis(340), monsterVisual);
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

    public static void animatePowerupActivate(StackPane cell, String text) {
        if (cell == null) return;

        animateFloatingText(cell, text, true);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(180), cell);
        pulse.setToX(1.18);
        pulse.setToY(1.18);
        pulse.setCycleCount(4);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    public static void animateEnergyPulse(StackPane cell) {
        if (cell == null) return;

        DropShadow glow = new DropShadow(25, Color.web("#00d4ff"));
        Node target = cell;
        target.setEffect(glow);

        FadeTransition fade = new FadeTransition(Duration.millis(650), target);
        fade.setFromValue(0.75);
        fade.setToValue(1.0);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.setOnFinished(e -> target.setEffect(null));
        fade.play();
    }

    /**
     * CARD DRAW POPUP: Glassmorphism Screen Overlay
     */
    /**
     * CARD DRAW POPUP: Interactive Flip (Click to Reveal, Click to Dismiss)
     */
    /**
     * CARD DRAW POPUP: Interactive Flip (Click to Reveal, Click to Dismiss)
     */
    public static void showCardPopup(StackPane rootPane, String cardName, String cardEffect, Runnable onClose) {
        // 1. Create the Dark Overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        // 2. Create the Card Container
        javafx.scene.layout.VBox cardVisual = new javafx.scene.layout.VBox(16);
        cardVisual.setAlignment(javafx.geometry.Pos.CENTER);
        cardVisual.setMaxSize(280, 390);
        cardVisual.setStyle(
                "-fx-background-color: #2c3e50; " +
                        "-fx-border-color: #ff00ff; -fx-border-width: 4; -fx-border-radius: 14; " +
                        "-fx-background-radius: 14; -fx-padding: 24;"
        );
        cardVisual.setEffect(new javafx.scene.effect.DropShadow(24, javafx.scene.paint.Color.MAGENTA));

        // 3. Create the "Back" of the card (Visible first)
        javafx.scene.control.Label backLabel = new javafx.scene.control.Label("?");
        backLabel.setStyle("-fx-font-size: 82px; -fx-text-fill: white; -fx-font-weight: bold;");
        javafx.scene.control.Label clickHint = new javafx.scene.control.Label("(Click to Reveal)");
        clickHint.setStyle("-fx-text-fill: gray; -fx-font-size: 13px;");

        javafx.scene.layout.VBox backContent = new javafx.scene.layout.VBox(8, backLabel, clickHint);
        backContent.setAlignment(javafx.geometry.Pos.CENTER);
        cardVisual.getChildren().add(backContent);

        // 4. Create the "Front" Content (Hidden initially)
        javafx.scene.layout.VBox frontContent = new javafx.scene.layout.VBox(14);
        frontContent.setAlignment(javafx.geometry.Pos.CENTER);

        javafx.scene.control.Label title = new javafx.scene.control.Label("CARD DRAWN!");
        title.setStyle("-fx-text-fill: #ff00ff; -fx-font-size: 21px; -fx-font-weight: bold;");

        javafx.scene.control.Label name = new javafx.scene.control.Label(cardName);
        name.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 22px; -fx-font-weight: bold;");
        name.setWrapText(true);
        name.setMaxWidth(230);
        name.setAlignment(javafx.geometry.Pos.CENTER);
        name.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.Label effect = new javafx.scene.control.Label(cardEffect);
        effect.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        effect.setWrapText(true);
        effect.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.Label closeHint = new javafx.scene.control.Label("(Click to Continue)");
        closeHint.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

        frontContent.getChildren().addAll(title, name, effect, closeHint);

        // 5. Add to Screen
        overlay.getChildren().add(cardVisual);
        rootPane.getChildren().add(overlay);

        // 6. Interactive Flip State Machine
        final boolean[] isRevealed = {false};

        Runnable advancePopup = () -> {
            if (!isRevealed[0]) {
                // --- FIRST CLICK: REVEAL ---
                isRevealed[0] = true;

                javafx.animation.ScaleTransition flipToEdge = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(250), cardVisual);
                flipToEdge.setToX(0);

                flipToEdge.setOnFinished(ev -> {
                    cardVisual.getChildren().clear();
                    cardVisual.getChildren().add(frontContent);

                    javafx.animation.ScaleTransition flipToFront = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(250), cardVisual);
                    flipToFront.setToX(1);
                    flipToFront.play();
                });

                flipToEdge.play();
            } else {
                // --- SECOND CLICK: DISMISS ---
                javafx.animation.ScaleTransition popOut = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(200), cardVisual);
                popOut.setToX(0);
                popOut.setToY(0);
                popOut.setOnFinished(event -> {
                    rootPane.getChildren().remove(overlay);

                    // --- THIS IS THE MAGIC 4TH ARGUMENT THAT UNFREEZES THE GAME! ---
                    if (onClose != null) {
                        onClose.run();
                    }
                });
                popOut.play();
            }
        };

        cardVisual.setOnMouseClicked(e -> {
            e.consume();
            advancePopup.run();
        });

        overlay.setOnMouseClicked(e -> {
            e.consume();
            advancePopup.run();
        });
    }
}
