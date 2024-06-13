package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * NextPieceListener is used for handle a piece user will play next
 */
public interface NextPieceListener {

    /**
     * Handle replacing and updating the next pieces
     * @param piece piece that player have to put next
     * @param followingPiece piece that player will need to put following to "piece" or swap
     */
    public void nextPiece(GamePiece piece, GamePiece followingPiece);
}
