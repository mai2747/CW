package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

public class PieceBoard extends GameBoard{

    public PieceBoard() {
        super(3, 3, 100, 100);
    }

    public PieceBoard(int width, int height) {
        super(3, 3, width, height);
    }
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);


    protected void build() {
        super.build();
        logger.info("Building piece board grid");

        setGridLinesVisible(true);

        for(var y = 0; y < 3; y++) {
            for (var x = 0; x < 3; x++) {
                createBlock(x,y);
            }
        }
    }

    public void setPiece(GamePiece piece) {
        clearBoard();

        for (int i = 0; i < piece.getBlocks().length; i++) {
            for (int j = 0; j < piece.getBlocks()[0].length; j++) {
                if (piece.getBlocks()[j][i] != 0) {
                    grid.set(j,i,piece.getBlocks()[j][i]);
                }
            }
        }
    }

    private void clearBoard() {
        // Clear the board by setting all blocks to value 0 (empty)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                    this.grid.set(j,i,0);
            }
        }
    }
}
