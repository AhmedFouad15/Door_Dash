package MainMenu;

import game.engine.Role;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

public class GameSetupWindow {

    public static void display() {
        Stage stage = SceneManager.getStage();
        if (stage != null && stage.getScene() != null && stage.getScene().getRoot() instanceof StackPane) {
            display((StackPane) stage.getScene().getRoot());
        } else {
            SceneManager.switchScene("MainMenu.fxml");
        }
    }

    public static void display(StackPane hostRoot) {
        if (hostRoot == null) return;
        GameController.scriptedDemoTargetWin = null;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.72);");
        overlay.setOpacity(0);

        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(620);
        panel.setMaxHeight(680);
        panel.setPadding(new Insets(24));
        panel.setStyle(
                "-fx-background-color: rgba(8, 18, 28, 0.96); " +
                        "-fx-border-color: #00d4ff; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-radius: 10;"
        );
        panel.setEffect(new DropShadow(28, Color.web("#00d4ff")));

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(createBrandHeader(), content);
        overlay.getChildren().add(panel);
        hostRoot.getChildren().add(overlay);
        overlay.setFocusTraversable(true);
        overlay.setOnKeyPressed(event -> {
            if (event.isControlDown() && (event.getCode() == KeyCode.W || event.getCode() == KeyCode.L)) {
                event.consume();
                toggleScriptedDemoSelection(overlay, event.getCode() == KeyCode.W);
            }
        });
        overlay.requestFocus();

        showModeSelection(content, overlay, hostRoot);
        animateIn(overlay, panel);
    }

    private static VBox createBrandHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView();
        URL logoUrl = GameSetupWindow.class.getResource("/MainMenu/assets/images/my_logo.png");
        if (logoUrl != null) {
            logo.setImage(new Image(logoUrl.toExternalForm()));
            logo.setFitHeight(82);
            logo.setPreserveRatio(true);
            logo.setEffect(new DropShadow(18, Color.web("#00d4ff")));
        }

        Label gameName = new Label("DoorDasH");
        gameName.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Scare vs Laugh Touchdown");
        subtitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffd700;");

        header.getChildren().addAll(logo, gameName, subtitle);
        return header;
    }

    private static void showModeSelection(VBox content, StackPane overlay, StackPane hostRoot) {
        content.getChildren().clear();

        Label title = new Label("Choose Game Mode");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label hint = new Label("Play solo against the computer or keep the classic two player setup.");
        hint.setStyle("-fx-text-fill: rgba(255,255,255,0.78); -fx-font-size: 13px;");
        hint.setWrapText(true);
        hint.setMaxWidth(500);
        hint.setAlignment(Pos.CENTER);
        hint.setTextAlignment(TextAlignment.CENTER);

        Button onePlayerBtn = createModeButton("1 Player", "#00d4ff", "#061018");
        Button twoPlayerBtn = createModeButton("2 Players", "#ffd700", "#101018");
        Button backBtn = createSecondaryButton("Back");

        HBox modeButtons = new HBox(18, onePlayerBtn, twoPlayerBtn);
        modeButtons.setAlignment(Pos.CENTER);

        onePlayerBtn.setOnAction(e -> {
            GameController.singlePlayerMode = true;
            GameController.playerDisplayName = "You";
            GameController.opponentDisplayName = "Computer";
            showInstructions(content, overlay, hostRoot);
        });

        twoPlayerBtn.setOnAction(e -> {
            GameController.singlePlayerMode = false;
            showNameEntry(content, overlay, hostRoot);
        });

        backBtn.setOnAction(e -> {
            GameController.scriptedDemoTargetWin = null;
            closeOverlay(overlay, hostRoot);
        });

        content.getChildren().addAll(title, hint, modeButtons, backBtn);
    }

    private static void showNameEntry(VBox content, StackPane overlay, StackPane hostRoot) {
        content.getChildren().clear();

        Label title = new Label("Player Names");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField playerNameField = createNameField("Player 1 name");
        TextField opponentNameField = createNameField("Player 2 name");

        Button continueBtn = createPrimaryButton("Continue");
        Button backBtn = createSecondaryButton("Back");

        HBox actions = new HBox(14, backBtn, continueBtn);
        actions.setAlignment(Pos.CENTER);

        continueBtn.setOnAction(e -> {
            GameController.playerDisplayName = cleanName(playerNameField.getText(), "Player 1");
            GameController.opponentDisplayName = cleanName(opponentNameField.getText(), "Player 2");
            showInstructions(content, overlay, hostRoot);
        });
        backBtn.setOnAction(e -> showModeSelection(content, overlay, hostRoot));

        content.getChildren().addAll(title, playerNameField, opponentNameField, actions);
    }

    private static void showInstructions(VBox content, StackPane overlay, StackPane hostRoot) {
        content.getChildren().clear();

        Label title = new Label("Game Setup");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        Label rulesText = new Label(
                "Welcome to Monstropolis, where every move is a race for energy and survival.\n\n" +
                        "Mission Brief\n" +
                        "DoorDasH is a race across Monstropolis. Each team collects energy, reacts to board events, and tries to reach the final door with enough power to win.\n\n" +
                        "Turn Flow\n" +
                        "1. Activate a power-up when you have at least 500 energy.\n" +
                        "2. Roll the dice and move to your landing cell.\n" +
                        "3. Resolve the cell: doors change energy, cards trigger surprises, conveyors move you forward, contamination socks pull you back, and stationed monsters can change the match.\n\n" +
                        "Board Rules\n" +
                        "1. A player cannot finish a move on the opponent's cell.\n" +
                        "2. Door energy can help matching roles and punish opposite roles.\n" +
                        "3. Shields block one negative energy effect, then break.\n\n" +
                        "Win Condition\n" +
                        "Reach cell 99 with at least 1000 energy."
        );
        rulesText.setWrapText(true);
        rulesText.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-line-spacing: 4px;");
        rulesText.setMaxWidth(520);

        ScrollPane scrollPane = new ScrollPane(rulesText);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setMaxWidth(540);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: rgba(0,212,255,0.35);");

        Button continueBtn = createPrimaryButton("Continue");
        Button cancelBtn = createSecondaryButton("Back");

        HBox actions = new HBox(14, cancelBtn, continueBtn);
        actions.setAlignment(Pos.CENTER);

        continueBtn.setOnAction(e -> showRoleSelection(content, overlay, hostRoot));
        cancelBtn.setOnAction(e -> showModeSelection(content, overlay, hostRoot));

        content.getChildren().addAll(title, scrollPane, actions);
    }

    private static void showRoleSelection(VBox content, StackPane overlay, StackPane hostRoot) {
        content.getChildren().clear();

        String chooser = GameController.singlePlayerMode
                ? GameController.playerDisplayName
                : cleanName(GameController.playerDisplayName, "Player 1");
        Label roleTitle = new Label(chooser + ", Choose Your Character");
        roleTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        roleTitle.setAlignment(Pos.CENTER);
        roleTitle.setTextAlignment(TextAlignment.CENTER);

        Label hint = new Label("Pick the team you want to play as this match.");
        hint.setStyle("-fx-text-fill: rgba(255,255,255,0.78); -fx-font-size: 13px;");
        hint.setAlignment(Pos.CENTER);
        hint.setTextAlignment(TextAlignment.CENTER);

        Button btnScarer = createRoleButton("SCARER", "#8a2be2", "white");
        Button btnLaugher = createRoleButton("LAUGHER", "#ffd700", "#101018");
        Button backBtn = createSecondaryButton("Back");

        HBox roleButtons = new HBox(18, btnScarer, btnLaugher);
        roleButtons.setAlignment(Pos.CENTER);

        btnScarer.setOnAction(e -> startGame(Role.SCARER, overlay, hostRoot));
        btnLaugher.setOnAction(e -> startGame(Role.LAUGHER, overlay, hostRoot));
        backBtn.setOnAction(e -> showInstructions(content, overlay, hostRoot));

        content.getChildren().addAll(roleTitle, hint, roleButtons, backBtn);
    }

    private static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(170);
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: #00d4ff; " +
                        "-fx-text-fill: #061018; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(130);
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12); " +
                        "-fx-border-color: rgba(255,255,255,0.35); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private static Button createRoleButton(String text, String background, String textColor) {
        Button button = new Button(text);
        button.setPrefWidth(190);
        button.setPrefHeight(70);
        button.setStyle(
                "-fx-background-color: " + background + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private static Button createModeButton(String text, String background, String textColor) {
        Button button = new Button(text);
        button.setPrefWidth(210);
        button.setPrefHeight(76);
        button.setStyle(
                "-fx-background-color: " + background + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-width: 0; " +
                        "-fx-focus-color: transparent; " +
                        "-fx-faint-focus-color: transparent; " +
                        "-fx-background-insets: 0; " +
                        "-fx-cursor: hand;"
        );
        return button;
    }

    private static TextField createNameField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(360);
        field.setPrefHeight(44);
        field.setStyle(
                "-fx-background-color: rgba(255,255,255,0.12); " +
                        "-fx-border-color: rgba(0,212,255,0.45); " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.55); " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-radius: 6;"
        );
        return field;
    }

    private static String cleanName(String name, String fallback) {
        if (name == null || name.trim().isEmpty()) return fallback;
        String trimmed = name.trim();
        return trimmed.length() > 18 ? trimmed.substring(0, 18) : trimmed;
    }

    private static void startGame(Role role, StackPane overlay, StackPane hostRoot) {
        GameController.playerRole = role;
        closeOverlay(overlay, hostRoot);
        SceneManager.switchScene("GameScreen.fxml");
    }

    private static void toggleScriptedDemoSelection(StackPane overlay, boolean winState) {
        boolean currentlySelected = GameController.scriptedDemoTargetWin != null && GameController.scriptedDemoTargetWin == winState;

        if (currentlySelected) {
            GameController.scriptedDemoTargetWin = null;
            showSetupToast(overlay, (winState ? "Win" : "Lose") + " State Disabled", "#ff003c");
            return;
        }

        GameController.scriptedDemoTargetWin = winState;
        showSetupToast(overlay, (winState ? "Win" : "Lose") + " State Activated", winState ? "#00d4ff" : "#ffd700");
    }

    private static void showSetupToast(StackPane overlay, String message, String accentColor) {
        Label toast = new Label(message);
        toast.setStyle(
                "-fx-background-color: rgba(8, 18, 28, 0.94); " +
                        "-fx-border-color: " + accentColor + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-padding: 10 14 10 14; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );
        toast.setEffect(new DropShadow(18, Color.web(accentColor)));
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new Insets(22, 22, 0, 0));
        toast.setOpacity(0);

        overlay.getChildren().removeIf(node -> "setup_scripted_demo_toast".equals(node.getId()));
        toast.setId("setup_scripted_demo_toast");
        overlay.getChildren().add(toast);
        toast.toFront();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(140), toast);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(1.8));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toast);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> overlay.getChildren().remove(toast));

        fadeIn.setOnFinished(event -> hold.play());
        hold.setOnFinished(event -> fadeOut.play());
        fadeIn.play();
    }

    private static void animateIn(StackPane overlay, Node panel) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), overlay);
        fade.setToValue(1);

        panel.setScaleX(0.92);
        panel.setScaleY(0.92);
        ScaleTransition scale = new ScaleTransition(Duration.millis(220), panel);
        scale.setToX(1);
        scale.setToY(1);

        fade.play();
        scale.play();
    }

    private static void closeOverlay(StackPane overlay, StackPane hostRoot) {
        FadeTransition fade = new FadeTransition(Duration.millis(140), overlay);
        fade.setToValue(0);
        fade.setOnFinished(e -> hostRoot.getChildren().remove(overlay));
        fade.play();
    }
}
