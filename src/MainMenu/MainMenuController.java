package MainMenu;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.animation.ScaleTransition;
import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.scene.effect.DropShadow;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.io.IOException;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.effect.GaussianBlur;
import java.util.Random;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import java.net.URL;

public class MainMenuController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private Button btnStart;
    @FXML private Button btnInstructions;
    @FXML private Button btnExit;
    @FXML private Label lblTitle;
    @FXML private Label Subtitle;
    @FXML private Pane particlePane;

    @FXML
    private void handleStartGame() {
        GameSetupWindow.display(rootPane);
    }

    private MediaPlayer backgroundMusicPlayer;
    private AudioClip hoverSound;
    private AudioClip clickSound;

    @FXML
    public void initialize() {



        bgImage.setPreserveRatio(false);
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        // 2. THE FAILSAFE: Wait for the UI to exist, then spawn
        Platform.runLater(() -> {
            createParticles();
        });

        // 3. BUTTONS (Clean Navigation via SceneManager)
        btnExit.setOnAction(e -> Platform.exit());

        btnStart.setOnAction(e -> {
            stopAudio(); // Stop the menu music
            GameSetupWindow.display(rootPane);
        });

        btnInstructions.setOnAction(e -> {
            stopAudio();
            SceneManager.switchScene("Instructions.fxml");
        });

        addHoverEffect(btnStart);
        addHoverEffect(btnInstructions);
        addHoverEffect(btnExit);

        createParticles();

        // --- 3. TITLE ANIMATION ---
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), lblTitle);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // --- 4. BACKGROUND ANIMATION ---
        ScaleTransition bgZoom = new ScaleTransition(Duration.seconds(20), bgImage);
        bgZoom.setToX(1.1);
        bgZoom.setToY(1.1);
        bgZoom.setAutoReverse(true);
        bgZoom.setCycleCount(Animation.INDEFINITE);
        bgZoom.play();

        try {
            // Adding the leading slash tells Java to look from the root of the project
            String imagePath = "/MainMenu/assets/images/background.jpg";
            URL res = getClass().getResource(imagePath);

            if (res != null) {
                bgImage.setImage(new Image(res.toExternalForm()));
                System.out.println("Background loaded from: " + imagePath);
            } else {
                System.out.println("CRITICAL: Image file not found at " + imagePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Load background music (loops forever)
            URL musicUrl = getClass().getResource("/MainMenu/assets/audio/background2.mp3");
            Media bgMedia = new Media(musicUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(bgMedia);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.3); // 30% volume so it's not too loud
            backgroundMusicPlayer.play();

            // Load UI sounds
            URL hoverUrl = getClass().getResource("/MainMenu/assets/audio/ui.mp3");
            hoverSound = new AudioClip(hoverUrl.toExternalForm());
            hoverSound.setVolume(0.5);

            URL clickUrl = getClass().getResource("/MainMenu/assets/audio/click.wav");
            clickSound = new AudioClip(clickUrl.toExternalForm());
            clickSound.setVolume(0.8);

        } catch (Exception e) {
            System.out.println("Could not load audio files! Check the folder/names.");
        }
    }

    private void stopAudio() {
        if (clickSound != null) clickSound.play();
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();
    }

    // --- UPGRADED HOVER METHOD ---
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

    // --- NEW: PARTICLE GENERATOR METHOD ---
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