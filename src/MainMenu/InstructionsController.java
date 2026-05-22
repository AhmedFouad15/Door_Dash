package MainMenu;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class InstructionsController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgImage;
    @FXML private Button btnBack;
    @FXML private Pane particlePane;
    @FXML private Label lblTitle;
    @FXML private VBox textContainer;

    private MediaPlayer backgroundMusicPlayer;
    private AudioClip hoverSound;
    private AudioClip clickSound;

    @FXML
    public void initialize() {
        Platform.runLater(this::createParticles);

        try {
            URL musicUrl = getClass().getResource("/MainMenu/assets/audio/background2.mp3");
            Media bgMedia = new Media(musicUrl.toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(bgMedia);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.3);
            backgroundMusicPlayer.play();

            URL hoverUrl = getClass().getResource("/MainMenu/assets/audio/UI.mp3");
            hoverSound = new AudioClip(hoverUrl.toExternalForm());
            hoverSound.setVolume(0.5);

            URL clickUrl = getClass().getResource("/MainMenu/assets/audio/click.wav");
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
        setupInstructionsText();
    }

    private void applyAnimations() {
        ScaleTransition bgZoom = new ScaleTransition(Duration.seconds(20), bgImage);
        bgZoom.setToX(1.1);
        bgZoom.setToY(1.1);
        bgZoom.setAutoReverse(true);
        bgZoom.setCycleCount(Animation.INDEFINITE);
        bgZoom.play();

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), lblTitle);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        addHoverEffect(btnBack);
    }

    private void addHoverEffect(Button button) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), button);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), button);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00d4ff"));
        glow.setRadius(20);
        glow.setSpread(0.4);

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
            pulse.setFromValue(0.2);
            pulse.setToValue(0.8);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);

            floatUp.play();
            pulse.play();
        }
    }

    private void setupInstructionsText() {
        textContainer.setAlignment(Pos.TOP_CENTER);

        addHeroImage("/MainMenu/assets/images/my_logo.png", 112);
        addHeader("DoorDasH: Scare vs Laugh Touchdown");
        addBody("DoorDasH is a competitive strategy board game set across the energy floors of Monstropolis. Two teams race through a 100-cell facility, collecting power from doors, triggering special rooms, and using monster abilities to control the pace of the match.\n\n" +
                "Every turn matters. Reaching the final cell is only half of the victory: a player must also build enough energy to finish the run.");

        addImageRow(
                "/MainMenu/assets/images/door_scarer.png",
                "/MainMenu/assets/images/door_laugher.png",
                "/MainMenu/assets/images/card_icon.png"
        );

        addHeader("Game Objective");
        addBody("Start at cell 0, move through the board, and be the first player to reach cell 99 with at least 1000 energy. If you arrive at the end without enough energy, the race continues until you can complete both win conditions.");

        addHeader("Choose Your Side");
        addBody("At setup, choose one of two roles:\n\n" +
                "SCARER: earns energy from scare doors and loses energy on laugh doors.\n\n" +
                "LAUGHER: earns energy from laugh doors and loses energy on scare doors.\n\n" +
                "After the role is selected, the game assigns a matching monster to the first player and an opposing-role monster to the rival player or computer.");

        addImageRow(
                "/MainMenu/assets/images/scarer_token.png",
                "/MainMenu/assets/images/laugher_token.png",
                "/MainMenu/assets/images/randall.png"
        );

        addHeader("Monster Abilities");
        addBody("Each monster has a class, a starting energy value, a passive trait, and a power-up. Power-ups cost energy, so strong timing can matter more than using an ability as soon as it is available.\n\n" +
                "Dashers move faster. Dynamos amplify energy swings. MultiTaskers trade movement for stronger energy control. Schemers pressure opponents by stealing energy across the board.");

        addHeader("Board Spaces");
        addSubHeader("Doors");
        addBody("Doors are the main source of energy. Matching doors reward the active monster and allied stationed monsters. Opposing doors punish the active monster. Each door can only be used once, then it remains marked as Used.");

        addSubHeader("Monster Cells");
        addBody("Stationed monsters create tactical encounters. Friendly encounters activate useful effects, while rival encounters can change energy totals and punish careless movement.");

        addSubHeader("Card Cells");
        addBody("Card cells draw from a 25-card deck. Cards can grant shields, steal energy, swap positions, restart a monster, or confuse both players. Once the deck is empty, it reloads and shuffles.");

        addSubHeader("Conveyors and Contamination Socks");
        addBody("Conveyors move a monster forward instantly. Contamination socks drag a monster backward and drain energy, turning a strong turn into a setback.");

        addHeader("Turn Flow");
        addBody("1. Activate a power-up if you have enough energy.\n" +
                "2. Roll the dice.\n" +
                "3. Move across the board.\n" +
                "4. Resolve the space where your monster lands.\n" +
                "5. Pass the turn to the other player.\n\n" +
                "A monster cannot finish movement on a cell occupied by the opponent.");

        addHeader("Winning");
        addBody("A player wins only when both conditions are true:\n\n" +
                "Reach cell 99.\n" +
                "Have at least 1000 energy.\n\n" +
                "DoorDasH rewards careful energy planning, smart power-up timing, and quick adaptation when cards or board spaces change the match.");
    }

    private void addHeader(String text) {
        Label header = new Label(text);
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FFD700; -fx-padding: 20 0 5 0;");
        header.setWrapText(true);
        header.setTextAlignment(TextAlignment.CENTER);
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(650);
        textContainer.getChildren().add(header);
    }

    private void addSubHeader(String text) {
        Label subHeader = new Label(text);
        subHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00d4ff; -fx-padding: 10 0 5 0;");
        subHeader.setWrapText(true);
        subHeader.setTextAlignment(TextAlignment.CENTER);
        subHeader.setAlignment(Pos.CENTER);
        subHeader.setMaxWidth(650);
        textContainer.getChildren().add(subHeader);
    }

    private void addBody(String text) {
        Label body = new Label(text);
        body.setStyle("-fx-font-size: 15px; -fx-text-fill: white; -fx-line-spacing: 5px;");
        body.setWrapText(true);
        body.setTextAlignment(TextAlignment.CENTER);
        body.setAlignment(Pos.CENTER);
        body.setMaxWidth(650);
        textContainer.getChildren().add(body);
    }

    private void addHeroImage(String path, double height) {
        ImageView imageView = createInstructionImage(path, height);
        if (imageView != null) {
            textContainer.getChildren().add(imageView);
        }
    }

    private void addImageRow(String... imagePaths) {
        HBox imageRow = new HBox(18);
        imageRow.setAlignment(Pos.CENTER);
        imageRow.setStyle("-fx-padding: 12 0 14 0;");

        for (String imagePath : imagePaths) {
            ImageView imageView = createInstructionImage(imagePath, 88);
            if (imageView != null) {
                imageRow.getChildren().add(imageView);
            }
        }

        if (!imageRow.getChildren().isEmpty()) {
            textContainer.getChildren().add(imageRow);
        }
    }

    private ImageView createInstructionImage(String path, double height) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) return null;

        ImageView imageView = new ImageView(new Image(imageUrl.toExternalForm()));
        imageView.setFitHeight(height);
        imageView.setFitWidth(height);
        imageView.setPreserveRatio(true);
        imageView.setEffect(new DropShadow(18, Color.web("#00d4ff", 0.55)));
        return imageView;
    }
}
