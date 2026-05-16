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
import javafx.scene.layout.VBox;
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

    // NEW: The VBox inside the ScrollPane where we will put the text
    @FXML private VBox textContainer;

    private MediaPlayer backgroundMusicPlayer;
    private AudioClip hoverSound;
    private AudioClip clickSound;

    @FXML
    public void initialize() {

        Platform.runLater(this::createParticles);

        // Load Sound
        try {
            URL musicUrl = getClass().getResource("assets/audio/background2.mp3");
            Media bgMedia = new Media(musicUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(bgMedia);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.3);
            backgroundMusicPlayer.play();

            URL hoverUrl = getClass().getResource("assets/audio/ui.mp3");
            hoverSound = new AudioClip(hoverUrl.toExternalForm());
            hoverSound.setVolume(0.5);

            URL clickUrl = getClass().getResource("assets/audio/click.wav");
            clickSound = new AudioClip(clickUrl.toExternalForm());
            clickSound.setVolume(0.8);
        } catch (Exception e) {
            System.out.println("Could not load audio files!");
        }

        bgImage.fitWidthProperty().bind(rootPane.widthProperty());
        bgImage.fitHeightProperty().bind(rootPane.heightProperty());

        btnBack.setOnAction(e -> {
            if (clickSound != null) clickSound.play();
            if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();
            SceneManager.switchScene("MainMenu.fxml");
        });

        applyAnimations();
        setupInstructionsText(); // Load the formatted text!
    }

    private void applyAnimations() {
        ScaleTransition bgZoom = new ScaleTransition(Duration.seconds(20), bgImage);
        bgZoom.setToX(1.1); bgZoom.setToY(1.1);
        bgZoom.setAutoReverse(true);
        bgZoom.setCycleCount(Animation.INDEFINITE);
        bgZoom.play();

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), lblTitle);
        pulse.setToX(1.05); pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        addHoverEffect(btnBack);
    }

    private void addHoverEffect(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), button);
        scaleIn.setToX(1.05); scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), button);
        scaleOut.setToX(1.0); scaleOut.setToY(1.0);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00d4ff"));
        glow.setRadius(20); glow.setSpread(0.4);

        button.setOnMouseEntered(e -> {
            if (hoverSound != null) hoverSound.play();
            scaleOut.stop();
            scaleIn.playFromStart();
            button.setEffect(glow);
        });

        button.setOnMouseExited(e -> {
            scaleIn.stop();
            scaleOut.playFromStart();
            button.setEffect(null);
        });
    }

    private void createParticles() {
        particlePane.setPrefSize(rootPane.getWidth(), rootPane.getHeight());
        Random rand = new Random();
        double width = rootPane.getWidth() <= 0 ? 800 : rootPane.getWidth();
        double height = rootPane.getHeight() <= 0 ? 600 : rootPane.getHeight();

        for (int i = 0; i < 80; i++) {
            Circle particle = new Circle(rand.nextDouble() * 5 + 2);
            particle.setFill(Color.web("#00d4ff", 0.5));
            particle.setEffect(new GaussianBlur(rand.nextDouble() * 5 + 5));

            particle.setTranslateX(rand.nextDouble() * width);
            particle.setTranslateY(height + (rand.nextDouble() * 200));
            particlePane.getChildren().add(particle);

            TranslateTransition floatUp = new TranslateTransition(Duration.seconds(rand.nextDouble() * 10 + 10), particle);
            floatUp.setByY(-(height + 400));
            floatUp.setCycleCount(Animation.INDEFINITE);

            FadeTransition pulse = new FadeTransition(Duration.seconds(rand.nextDouble() * 2 + 2), particle);
            pulse.setFromValue(0.2); pulse.setToValue(0.8);
            pulse.setAutoReverse(true); pulse.setCycleCount(Animation.INDEFINITE);

            floatUp.play(); pulse.play();
        }
    }

    // ==========================================
    // TEXT FORMATTING LOGIC
    // ==========================================

    private void setupInstructionsText() {
        addHeader("🎮 DooR DasH: Scare vs Laugh Touchdown");
        addBody("Welcome to DooR DasH, a fun and competitive board game set in the world of monsters!\n" +
                "In this game, monsters collect energy from children to power their world. Some monsters use scares, while others use laughter. Your mission is to control a monster, move across the board, collect energy, and defeat your opponent.\n\n" +
                "But winning is not just about reaching the end — you must also manage your energy carefully.");

        addHeader("🎭 Step 1: Choose Your Side");
        addBody("At the start of the game, you must choose your role:\n\n" +
                "👻 SCARER\n" +
                "• Uses fear and screams to collect energy\n" +
                "• Follows the traditional way of powering the city\n\n" +
                "😂 LAUGHER\n" +
                "• Uses laughter to collect energy\n" +
                "• A newer and more powerful method\n\n" +
                "After choosing your side, you will be assigned a random monster from that role, and your opponent will get a monster from the opposite role.");

        addHeader("👾 Your Monster");
        addBody("Each monster in the game is unique and has:\n\n" +
                "🔹 A Type: Affects how your monster behaves (speed, energy, strategy).\n" +
                "🔹 A Special Ability: Activated during your turn (costs energy). Can freeze opponents, steal energy, or move faster.\n" +
                "🔹 Starting Energy: Each monster begins with a different amount of energy.");

        addHeader("🧱 The Game Board");
        addBody("The game board has 100 cells, arranged in a zigzag pattern. You start at cell 0 and aim to reach cell 99. Each cell type has a different effect:");

        addSubHeader("🚪 Door Cells (Most Important)");
        addBody("✔ If the door matches your role: You and your team gain energy.\n" +
                "❌ If the door does NOT match: You and your team lose energy.\n" +
                "⚠️ Important: Each door can only be used once.");

        addSubHeader("👾 Monster Cells (Blue)");
        addBody("✔ Matches role: Activate power for FREE.\n" +
                "❌ Different role: You may swap energy with that monster depending on your balance.");

        addSubHeader("🎴 Card Cells (Red)");
        addBody("Draw a random card to swap positions, steal energy, restart, shield, or confuse.\n" +
                "⚠️ Important: Cards are used once and sometimes affect both players.");

        addSubHeader("⚙️ Conveyor Belts (Green) & 🧦 Contamination Socks (Orange)");
        addBody("Belts move you forward instantly. Socks move you backward and drain your energy!");

        addHeader("🎲 Turn System (How to Play)");
        addBody("Each turn follows these steps:\n" +
                "1. Power-Up (Optional): Activate ability (costs energy).\n" +
                "2. Roll the Dice: Move forward 1 to 6 spaces.\n" +
                "3. Land on a Cell: Trigger the cell's specific effect.\n" +
                "   • You cannot land on a cell occupied by the opponent.\n" +
                "4. End Turn: The next player plays.");

        addHeader("⚡ Energy System (Very Important)");
        addBody("Energy is crucial. Gain it via correct doors and cards. Lose it via wrong doors, socks, or opponent attacks.\n\n" +
                "⚠️ IMPORTANT RULE: You CANNOT win without enough energy, even if you reach the final cell!");

        addHeader("🏆 Winning the Game");
        addBody("To win, you must meet BOTH conditions:\n" +
                "✔ Reach the final cell (cell 99)\n" +
                "✔ Have at least 1000 energy\n\n" +
                "❗ If you reach the end without enough energy, you must keep playing and move around the board again.");

        addHeader("🎬 Final Words");
        addBody("This is not just a race... It’s a battle of strategy, timing, and decision-making.\n" +
                "Will you win using fear? 👻 Or laughter? 😂\n\n" +
                "The choice is yours.");
    }

    // Helper to format Main Headers (Gold)
    private void addHeader(String text) {
        Label header = new Label(text);
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-padding: 20 0 5 0;");
        header.setWrapText(true);
        textContainer.getChildren().add(header);
    }

    // Helper to format Subheaders (Cyan)
    private void addSubHeader(String text) {
        Label subHeader = new Label(text);
        subHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00d4ff; -fx-padding: 10 0 5 0;");
        subHeader.setWrapText(true);
        textContainer.getChildren().add(subHeader);
    }

    // Helper to format Body Text (White)
    private void addBody(String text) {
        Label body = new Label(text);
        body.setStyle("-fx-font-size: 15px; -fx-text-fill: white; -fx-line-spacing: 5px;");
        body.setWrapText(true);
        textContainer.getChildren().add(body);
    }
}