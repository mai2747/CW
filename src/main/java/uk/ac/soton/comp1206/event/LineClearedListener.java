package uk.ac.soton.comp1206.event;

/**
 * LineClearedListener is used for listening to the timing of any lines are cleared
 */

public interface LineClearedListener {
    /**
     * Handle event to set animation for fadeOut
     * @param x send column to be cleared
     * @param y send row to be cleared
     */
    public void lineCleared(int x, int y);
}
