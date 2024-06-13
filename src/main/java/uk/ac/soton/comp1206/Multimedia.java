package uk.ac.soton.comp1206;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.InstructionScene;

import java.util.Objects;

/**
 * Handle playing/stopping music
 */

public class Multimedia {
    private MediaPlayer audioPlayer;
    private MediaPlayer musicPlayer;

    private static final Logger logger = LogManager.getLogger(InstructionScene.class);

    public void playAudio(String audioFilePath) {
        Media audio = new Media(Objects.requireNonNull(getClass().getResource(audioFilePath)).toString());
        audioPlayer = new MediaPlayer(audio);
        audioPlayer.play();
    }

    public void playMusic(String musicFilePath) {
        Media music = new Media(Objects.requireNonNull(getClass().getResource(musicFilePath)).toString());
        musicPlayer = new MediaPlayer(music);
        musicPlayer.setOnEndOfMedia(() -> musicPlayer.seek(Duration.ZERO)); // Loop the music
        musicPlayer.play();
    }

    public void stopMusic() {
        logger.info("MusicPlayer: " + musicPlayer);
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}
