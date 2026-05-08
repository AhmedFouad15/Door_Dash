package MainMenu;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    @FXML private StackPane rootPane; // Grabbed from MyController
    @FXML private ImageView bgImage;  // This handles both resizing AND animation now
    @FXML private Button btnStart;
    @FXML private Button btnInstructions;
    @FXML private Button btnExit;
    @FXML private Label lblTitle;
    @FXML private Label Subtitle;
    @FXML private Pane particlePane;

    private MediaPlayer backgroundMusicPlayer;
    private AudioClip hoverSound;
    private AudioClip clickSound;

    @FXML
    public void initialize() {
        // --- 1. DYNAMIC RESIZE (From MyController) ---
        // Binds the background image size to the window size
        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        // --- 2. BUTTON ACTIONS ---
        btnExit.setOnAction(e -> Platform.exit());

        btnStart.setOnAction(e -> {
            if (clickSound != null) clickSound.play();
            if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();

            // The SceneManager already handles the Fade Out and the Fade In!
            SceneManager.switchScene("Game.fxml");
        });

        btnInstructions.setOnAction(e -> {
            if (clickSound != null) clickSound.play();

            if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();
            // Keep music playing for instructions if you want, or stop it here
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

        } catch (Exception e) {
            System.out.println("Could not load audio files! Check the folder/names.");
        }
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

    // --- SCENE TRANSITION METHODS ---

    private void fadeOutToGame() {
        // 1. Create a fade out animation for the entire root pane (takes 0.5 seconds)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), rootPane);
        fadeOut.setFromValue(1.0); // 100% visible
        fadeOut.setToValue(0.0);   // 0% visible (invisible)

        // 2. Tell Java exactly what to do when the fade finishes
        fadeOut.setOnFinished(event -> loadNextScene());

        // 3. Start the fade!
        fadeOut.play();
    }

    private void loadNextScene() {
        try {
            Parent gameRoot = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));

            // 2. Start the new screen at 0% opacity so we can fade it in
            gameRoot.setOpacity(0.0);

            // 3. Grab the current window and swap the scene
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(gameRoot, 800, 600)); // Keep the same window size

            // 4. Fade the new scene in!
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), gameRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not load the next scene! Check the file name.");
        }
    }

    // --- NEW: PARTICLE GENERATOR METHOD ---
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