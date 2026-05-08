package MainMenu;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class InstructionsController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private Button btnBack;
    @FXML private Pane particlePane;
    @FXML private Label lblTitle;

    private MediaPlayer backgroundMusicPlayer;
    private AudioClip hoverSound;
    private AudioClip clickSound;

    @FXML
    public void initialize() {
        // Load Sound
        try {
            // Load background music (loops forever)
            URL musicUrl = getClass().getResource("assets/audio/background2.mp3");
            Media bgMedia = new Media(musicUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(bgMedia);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.3); // 30% volume so it's not too loud
            backgroundMusicPlayer.play();

            // Load UI sounds
            URL hoverUrl = getClass().getResource("assets/audio/ui.mp3");
            hoverSound = new AudioClip(hoverUrl.toExternalForm());
            hoverSound.setVolume(0.5);

            URL clickUrl = getClass().getResource("assets/audio/click.wav");
            clickSound = new AudioClip(clickUrl.toExternalForm());
            clickSound.setVolume(0.8);
        }
        catch (Exception e) {
        System.out.println("Could not load audio files! Check the folder/names.");
    }

        // Bind Background
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        // Navigation - Simple and clean thanks to SceneManager
        btnBack.setOnAction(e -> {
            if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();
            SceneManager.switchScene("MainMenu.fxml");
        });

        applyAnimations();
        createParticles(); // Keep the vibe consistent!
    }

    private void applyAnimations() {
        // Slow BG Zoom (Consistency!)
        ScaleTransition bgZoom = new ScaleTransition(Duration.seconds(20), bgImage);
        bgZoom.setToX(1.1); bgZoom.setToY(1.1);
        bgZoom.setAutoReverse(true);
        bgZoom.setCycleCount(Animation.INDEFINITE);
        bgZoom.play();

        // Title Pulse
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), lblTitle);
        pulse.setToX(1.05); pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // Use the same hover logic for the Back button
        addHoverEffect(btnBack);
    }

    private void addHoverEffect(Button button) {
        // 1. The Growth Animations (150ms)
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), button);
        scaleIn.setToX(1.05); // Grows 5%
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), button);
        scaleOut.setToX(1.0); // Returns to normal
        scaleOut.setToY(1.0);

        // 2. The Glow Effect
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00d4ff"));
        glow.setRadius(20);
        glow.setSpread(0.4); // Makes it a bright, solid glow

        // 3. What happens when the mouse ENTERS
        button.setOnMouseEntered(e -> {
            // If you added the audio step earlier, play the tick sound here!
            if (hoverSound != null) hoverSound.play();

            scaleOut.stop(); // Prevent glitching
            scaleIn.playFromStart();
            button.setEffect(glow); // Turn on the glow
        });

        // 4. What happens when the mouse EXITS
        button.setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.playFromStart();
            button.setEffect(null); // Turn off the glow
        });
    }

    private void createParticles() {
        Random rand = new Random();

        // Create 30 floating particles
        for (int i = 0; i < 30; i++) {
            // 1. Create a circle with a random size (radius between 2 and 7)
            Circle particle = new Circle(rand.nextDouble() * 5 + 2);

            // 2. Make it a semi-transparent glowing purple (matches your subtitle!)
            particle.setFill(Color.web("#00d4ff", 0.5));

            // 3. Add a blur effect to make it look like glowing light/smoke
            particle.setEffect(new GaussianBlur(rand.nextDouble() * 5 + 5));

            // 4. Set random starting positions
            // X: anywhere across the 800px width
            // Y: anywhere from the bottom of the screen (600) down to 1200px below it
            double startX = rand.nextDouble() * 800;
            double startY = rand.nextDouble() * 600 + 600;
            particle.setTranslateX(startX);
            particle.setTranslateY(startY);

            // 5. Add the particle to our invisible pane
            particlePane.getChildren().add(particle);

            // --- ANIMATIONS ---

            // Float upwards animation (Takes 10 to 20 seconds to reach the top)
            TranslateTransition floatUp = new TranslateTransition(Duration.seconds(rand.nextDouble() * 10 + 10), particle);
            floatUp.setByY(-1000); // Move 1000 pixels straight up
            floatUp.setCycleCount(Animation.INDEFINITE); // Keep doing it forever

            // Pulse (Fade in and out) animation (Takes 2 to 4 seconds)
            FadeTransition pulse = new FadeTransition(Duration.seconds(rand.nextDouble() * 2 + 2), particle);
            pulse.setFromValue(0.2); // Dim
            pulse.setToValue(0.8);   // Bright
            pulse.setAutoReverse(true); // Smoothly fade back down
            pulse.setCycleCount(Animation.INDEFINITE);

            // Start both animations for this specific particle!
            floatUp.play();
            pulse.play();
        }
    }
}