package MainMenu;

import game.engine.monsters.*;
import game.engine.Role;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.animation.Interpolator;

public class MonsterRenderer {

    private static final Image SULLEY_IMAGE = loadImage("/MainMenu/assets/images/scarer_token.png");
    private static final Image MIKE_IMAGE = loadImage("/MainMenu/assets/images/laugher_token.png");
    private static final Image RANDALL_IMAGE = loadImage("/MainMenu/assets/images/randall.png");
    private static final Image CELIA_IMAGE = loadImage("/MainMenu/assets/images/celia.png");
    private static final Image HENRY_IMAGE = loadImage("/MainMenu/assets/images/Henry.png");
    private static final Image ROZ_IMAGE = loadImage("/MainMenu/assets/images/roz.png");
    private static final Image FUNGUS_IMAGE = loadImage("/MainMenu/assets/images/fungus.png");
    private static final Image YETI_IMAGE = loadImage("/MainMenu/assets/images/yeti.png");

    public static StackPane createMonsterVisual(Monster monster, boolean isActiveTurn) {
        StackPane monsterNode = new StackPane();
        monsterNode.setMouseTransparent(true);
        monsterNode.setId("monster_visual");

        // 1. BASE AVATAR (Image only - Text removed)
        ImageView avatar = new ImageView();
        avatar.setFitWidth(45);
        avatar.setFitHeight(45);
        avatar.setPreserveRatio(true);

        avatar.setImage(getMonsterImage(monster));

        // 2. STATUS EFFECTS (Shield, Confusion, etc.)
        DropShadow glowEffect = new DropShadow();
        glowEffect.setRadius(isActiveTurn ? 20 : 0);
        glowEffect.setColor(isActiveTurn ? Color.GOLD : Color.TRANSPARENT);

        if (monster.isShielded()) {
            glowEffect.setRadius(20);
            glowEffect.setColor(Color.AQUA);
            glowEffect.setSpread(0.6);
        }

        avatar.setEffect(glowEffect);
        monsterNode.getChildren().add(avatar);
        monsterNode.getChildren().add(createTypeBadge(monster));

        // 3. FROZEN OVERLAY
        if (monster.isFrozen()) {
            StackPane iceOverlay = new StackPane();
            iceOverlay.setStyle("-fx-background-color: rgba(173, 216, 230, 0.4); -fx-background-radius: 10;");
            iceOverlay.setPrefSize(40, 40);
            monsterNode.getChildren().add(iceOverlay);
        }

        if (monster.getConfusionTurns() > 0 || monster.getRole() != monster.getOriginalRole()) {
            Label confusionBadge = new Label("?");
            confusionBadge.setMouseTransparent(true);
            confusionBadge.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-color: rgba(255, 0, 255, 0.85); " +
                            "-fx-padding: 0 5 0 5; -fx-background-radius: 9;"
            );
            StackPane.setAlignment(confusionBadge, javafx.geometry.Pos.TOP_RIGHT);
            monsterNode.getChildren().add(confusionBadge);
        }

        // 4. AMBIENT MOTION
        if (isActiveTurn) {
            TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(1.5), monsterNode);
            floatAnim.setByY(-6);
            floatAnim.setAutoReverse(true);
            floatAnim.setCycleCount(Animation.INDEFINITE);
            floatAnim.setInterpolator(Interpolator.EASE_BOTH);
            floatAnim.play();
        }

        return monsterNode;
    }

    private static Label createTypeBadge(Monster monster) {
        Label badge = new Label(getBadgeText(monster));
        badge.setMouseTransparent(true);
        badge.setStyle(
                "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                        "-fx-background-color: " + getBadgeColor(monster) + "; " +
                        "-fx-padding: 1 4 1 4; -fx-background-radius: 7;"
        );
        StackPane.setAlignment(badge, javafx.geometry.Pos.BOTTOM_RIGHT);
        return badge;
    }

    private static String getBadgeText(Monster monster) {
        if (monster instanceof Dasher) return "x2";
        if (monster instanceof Dynamo) return "2E";
        if (monster instanceof MultiTasker) return "+200";
        if (monster instanceof Schemer) return "+10";
        return "?";
    }

    private static String getBadgeColor(Monster monster) {
        if (monster instanceof Dasher) return "rgba(0, 212, 255, 0.9)";
        if (monster instanceof Dynamo) return "rgba(255, 215, 0, 0.9)";
        if (monster instanceof MultiTasker) return "rgba(76, 175, 80, 0.9)";
        if (monster instanceof Schemer) return "rgba(156, 39, 176, 0.9)";
        return "rgba(90, 90, 90, 0.9)";
    }

    public static Image getMonsterImage(Monster monster) {
        String name = monster.getName();
        if ("James P. Sullivan".equals(name)) return SULLEY_IMAGE;
        if ("Mike Wazowski".equals(name)) return MIKE_IMAGE;
        if ("Randall Boggs".equals(name)) return RANDALL_IMAGE;
        if ("Celia Mae".equals(name)) return CELIA_IMAGE;
        if ("Henry J. Waternoose".equals(name)) return HENRY_IMAGE;
        if ("Roz".equals(name)) return ROZ_IMAGE;
        if ("Fungus".equals(name)) return FUNGUS_IMAGE;
        if ("Yeti".equals(name)) return YETI_IMAGE;
        return monster.getOriginalRole() == Role.SCARER ? SULLEY_IMAGE : MIKE_IMAGE;
    }

    private static Image loadImage(String path) {
        try {
            return new Image(MonsterRenderer.class.getResourceAsStream(path));
        } catch (Exception e) {
            return null;
        }
    }
}
