package MainMenu;

import game.engine.monsters.Monster;
import game.engine.Role;
import javafx.scene.effect.DropShadow;
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

    public static StackPane createMonsterVisual(Monster monster, boolean isActiveTurn) {
        StackPane monsterNode = new StackPane();
        monsterNode.setMouseTransparent(true);
        monsterNode.setId("monster_visual");

        // 1. BASE AVATAR (Image only - Text removed)
        ImageView avatar = new ImageView();
        avatar.setFitWidth(45);
        avatar.setFitHeight(45);
        avatar.setPreserveRatio(true);

        try {
            String path = (monster.getRole() == Role.SCARER)
                    ? "/MainMenu/assets/images/scarer_token.png"
                    : "/MainMenu/assets/images/laugher_token.png";
            avatar.setImage(new Image(MonsterRenderer.class.getResourceAsStream(path)));
        } catch (Exception e) {
            // Placeholder color if asset missing
        }

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

        // 3. FROZEN OVERLAY
        if (monster.isFrozen()) {
            StackPane iceOverlay = new StackPane();
            iceOverlay.setStyle("-fx-background-color: rgba(173, 216, 230, 0.4); -fx-background-radius: 10;");
            iceOverlay.setPrefSize(40, 40);
            monsterNode.getChildren().add(iceOverlay);
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
}