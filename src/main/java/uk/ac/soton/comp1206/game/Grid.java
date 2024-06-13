package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.scene.MenuScene;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for its display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;
    private static final Logger logger = LogManager.getLogger(Grid.class);
    Multimedia multimedia = new Multimedia();


    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    public void playPiece(GamePiece piece, int x, int y){
        logger.info("--placing piece of " + piece);

        for (int i = 0; i < piece.getBlocks().length; i++) {
            for (int j = 0; j < piece.getBlocks()[0].length; j++) {
                int gridX = x + i -1;
                int gridY = y + j -1;
                if (piece.getBlocks()[i][j] > 0) {
                    grid[gridX][gridY].set(piece.getValue());
                }
            }
        }
    }

    public boolean canPlayPiece(GamePiece piece, int x, int y){
        for (int i = 0; i < piece.getBlocks().length; i++) {
            for (int j = 0; j < piece.getBlocks()[0].length; j++) {
                if(piece.getBlocks()[i][j] > 0){
                    int gridX = x + i - 1;
                    int gridY = y + j - 1;

                    if(gridX < 0 || gridY < 0 || gridX >= cols || gridY >= rows || grid[gridX][gridY].get() > 0){
                        logger.info("fail to place");
                        multimedia.playAudio("/sounds/fail.wav");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

}
