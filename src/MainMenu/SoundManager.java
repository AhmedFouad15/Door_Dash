package MainMenu;

import javafx.scene.media.AudioClip;
import game.engine.Role;
import java.net.URL;

public class SoundManager {

    private static AudioClip diceSound;
    private static AudioClip moveSound;
    private static AudioClip errorSound;
    private static AudioClip cardSound;
    private static AudioClip damageSound;
    private static AudioClip victorySound;
    private static AudioClip loseSound;
    private static AudioClip scareDoorSound;
    private static AudioClip laughDoorSound;
    private static AudioClip contaminationSound;
    private static AudioClip lose_game;
    private static AudioClip happySound;

    // Call this ONCE in GameController.initialize()
    public static void init() {
        // Updated to match your actual filenames in assets/audio/
        diceSound = loadSound("click.wav");    // Using click for dice
        damageSound = loadSound("UI.mp3");     // Using UI for damage/events
        cardSound = loadSound("UI.mp3");       // Using UI for cards
        errorSound = loadSound("click.wav");   // Using click for errors
        moveSound = loadSound("click.wav");// Using click for movement
        loseSound = loadSound("lose_sound.mp3");
        scareDoorSound = loadOptionalSound("scare.m4a");
        laughDoorSound = loadOptionalSound("laugh.m4a");
        contaminationSound = loadOptionalSound("2319.mp3");
        lose_game = loadOptionalSound("lose_game.mp3");
        happySound = loadOptionalSound("happy.mp3");
    }

        // Note: background2.mp3 and background.mp3 are long files,
        // they should be played via MediaPlayer, not AudioClip.

    private static AudioClip loadSound(String fileName) {
        try {
            // Updated path to ensure it finds the assets correctly
            String path = "/MainMenu/assets/audio/" + fileName;
            URL url = SoundManager.class.getResource(path);

            if (url == null) {
                System.out.println("AUDIO ERROR: Could not find file at " + path);
                return null;
            }

            return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            System.out.println("AUDIO ERROR: Found file but could not play " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    public static void playDice() { if (diceSound != null) diceSound.play(0.2); }
    public static void playMove() { if (moveSound != null) moveSound.play(0.2); }
    public static void playError() { if (errorSound != null) errorSound.play(0.5); }
    public static void playCard() { if (cardSound != null) cardSound.play(); }
    public static void playDamage() { if (damageSound != null) damageSound.play(); }
    public static void playVictory() { if (victorySound != null) victorySound.play(); }
    public static void playLose() { if (loseSound != null) loseSound.play(); }
    public static void playLose_game(){if(lose_game!=null) lose_game.play();}
    public static void playDoor(Role role) {
        AudioClip doorSound = role == Role.SCARER ? scareDoorSound : laughDoorSound;
        if (doorSound != null) {
            doorSound.play();
        } else {
            playCard();
        }
    }
    public static void playContamination() {
        if (contaminationSound != null) {
            contaminationSound.play();
        } else {
            playLose();
        }
    }

    public static void playHappy() {
        if (happySound != null) {
            happySound.play();
        } else {
            playCard();
        }
    }

    private static AudioClip loadOptionalSound(String fileName) {
        try {
            URL url = SoundManager.class.getResource("/MainMenu/assets/audio/" + fileName);
            return url == null ? null : new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            return null;
        }
    }
}
