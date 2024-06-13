package uk.ac.soton.comp1206.event;

/**
 * GameLoopListener is used for listening to game loop when it needs to be repeated
 */
public interface GameLoopListener {

    /**
     * Handle a loop of a game
     * @param delay time player can use for one piece
     */
    public void setOnGameLoop(int delay);
}
