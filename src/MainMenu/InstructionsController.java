package MainMenu;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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

        // 2. THE FAILSAFE: Wait for the UI to exist, then spawn
        Platform.runLater(() -> {
            createParticles();
        });

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
        // Force the particlePane to match the window size
        particlePane.setPrefSize(rootPane.getWidth(), rootPane.getHeight());

        Random rand = new Random();
        double width = rootPane.getWidth();
        double height = rootPane.getHeight();

        // Safety check: if width is still 0, use a default so it doesn't crash
        if (width <= 0) width = 800;
        if (height <= 0) height = 600;

        for (int i = 0; i < 80; i++) {
            Circle particle = new Circle(rand.nextDouble() * 5 + 2);
            particle.setFill(Color.web("#00d4ff", 0.5));
            particle.setEffect(new GaussianBlur(rand.nextDouble() * 5 + 5));

            // Spawn across the FULL width and below the FULL height
            double startX = rand.nextDouble() * width;
            double startY = height + (rand.nextDouble() * 200);

            particle.setTranslateX(startX);
            particle.setTranslateY(startY);
            particlePane.getChildren().add(particle);

            // Animation: Ensure it travels the WHOLE height of the window
            TranslateTransition floatUp = new TranslateTransition(
                    Duration.seconds(rand.nextDouble() * 10 + 10), particle
            );
            floatUp.setByY(-(height + 400));
            floatUp.setCycleCount(Animation.INDEFINITE);

            FadeTransition pulse = new FadeTransition(
                    Duration.seconds(rand.nextDouble() * 2 + 2), particle
            );
            pulse.setFromValue(0.2);
            pulse.setToValue(0.8);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);

            floatUp.play();
            pulse.play();
        }
    }
}