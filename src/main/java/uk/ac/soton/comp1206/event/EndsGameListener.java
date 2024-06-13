package uk.ac.soton.comp1206.event;

/**
 * EndsGameListener is used for listening to the time to end game (when player' game is over)
 */
public interface EndsGameListener {
    /**
     * inform game over from Game class to ChallengeScene
     */
    void gameOver();
}
