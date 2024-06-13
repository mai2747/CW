package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.EndsGameListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game{

    private static final Logger logger = LogManager.getLogger(Game.class);
    protected final int rows;
    protected final int cols;

    //The grid model linked to the game
    protected final Grid grid;
    Random random = new Random();
    GamePiece currentPiece;
    GamePiece followingPiece;

    protected ScheduledFuture<?> loop;
    protected ScheduledExecutorService time;

    Multimedia multimedia = new Multimedia();
    protected LineClearedListener lineClearedListener = null;
    protected GameLoopListener gameLoopListener = null;
    protected EndsGameListener endsGameListener = null;

    protected final IntegerProperty score = new SimpleIntegerProperty(0);
    protected final IntegerProperty level = new SimpleIntegerProperty(0);
    protected final IntegerProperty life = new SimpleIntegerProperty(3);
    protected final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        this.currentPiece = spawnPiece();

        time = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        logger.info("Starting game");
        initialiseGame();

        loop = time.schedule(this::gameLoop, getTimerDelay(),TimeUnit.MILLISECONDS);

        gameLoopListener();
    }

    public GamePiece spawnPiece(){
        return GamePiece.createPiece(random.nextInt(15), random.nextInt(4)); //bound = given number??
    }

    //ここからjp
    private final List<NextPieceListener> nextPieceListeners = new ArrayList<>();
    public void setNextPieceListener(NextPieceListener listener){
        nextPieceListeners.add(listener);
    }
    public void registerNextPieceListener(NextPieceListener listener) {
        nextPieceListeners.add(listener);
    }
    private void notifyNextPieceListeners(GamePiece piece) {
        for (NextPieceListener listener : nextPieceListeners) {  //if(NextPieceListener != null){}?
            listener.nextPiece(piece,followingPiece);  //pieceなのかcurrentPieceなのかjp
        }
    }
    public void setLineClearedListener(LineClearedListener lineClearedListener){
        this.lineClearedListener = lineClearedListener;
    }
    public void setOnGameLoop(GameLoopListener gameLoopListener) {
        this.gameLoopListener = gameLoopListener;
    }
    public void gameLoopListener() {
        if(gameLoopListener != null) gameLoopListener.setOnGameLoop(getTimerDelay());
    }

    public void endsTimer(){
        this.time.shutdownNow();
    }

    public void EndsGame(EndsGameListener endsGameListener){
        this.endsGameListener = endsGameListener;
    }

    public void gameOver(){
        logger.info("Game over");
        if(endsGameListener != null){
            Platform.runLater(() -> endsGameListener.gameOver());
        }
    }

    /**
     * Handle updating next pieces on piece board
     */
    public void nextPiece(){
        logger.info("next piece");
        currentPiece = followingPiece;

        followingPiece = spawnPiece();

        for(NextPieceListener listener : nextPieceListeners){
            listener.nextPiece(currentPiece,followingPiece);
        }
    }

    /**
     * Handle swapping current piece and following piece
     */
    public void swapCurrentPiece(){
        var changedPiece = currentPiece;
        currentPiece = followingPiece;
        followingPiece = changedPiece;
        notifyNextPieceListeners(currentPiece);
        multimedia.playAudio("/sounds/pling.wav");
    }

    /**
     * Handle rotation of current piece
     * @param rotation times to rotate the current piece
     */
    public void rotateCurrentPiece(int rotation){
        logger.info("Rotate piece");
        currentPiece.rotate(rotation);
    }

    /**
     * Handle calculation of score that player got from current placing
     * @param lines lines cleared
     * @param blocks blocks cleared
     */
    public void score(int lines, int blocks){
        int points = lines * blocks * 10 * getMultiplier();
        setScore(getScore() + points);

        int newLevel = getScore() / 1000;
        setLevel(newLevel);
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        this.score.set(0);
        this.level.set(0);
        this.life.set(3);
        this.multiplier.set(1);

        followingPiece = spawnPiece();
        notifyNextPieceListeners(currentPiece);
    }

    /**
     * get time limit corresponding to current level
     * @return max time to play each piece
     */
    public int getTimerDelay(){
        return Math.max(12000-500*level.get(), 2500);
    }

    /**
     * Handle game loop
     */
    public void gameLoop(){
        multimedia.playAudio("/sounds/fail.wav");
        if(life.get() > 0){
            life.set(life.get()-1);
            multiplier.set(1);
            logger.info("inside loop, life: "+getLife());
            logger.info(getMultiplier());

        }else gameOver();
        nextPiece();

        gameLoopListener();

        loop = time.schedule(this::gameLoop, getTimerDelay(),TimeUnit.MILLISECONDS);
    }


    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        if(getGrid().canPlayPiece(currentPiece,x,y)){
            multimedia.playAudio("/sounds/place.wav");
            grid.playPiece(currentPiece,x,y);

            afterPiece();
            nextPiece();

            loop.cancel(false);
            loop = time.schedule(this::gameLoop, getTimerDelay(),TimeUnit.MILLISECONDS);
            gameLoopListener();
        }
    }

    /**
     * Delete lines if any lines are filled with blocks
     */
    public void afterPiece(){
        int line = 0;
        Set<Integer> row = new HashSet<>();
        Set<Integer> col = new HashSet<>();

        for(int x = 0; x < getGrid().getCols(); x++){
            boolean colFull = true;
            for(int y = 0; y < getGrid().getRows(); y++){
                if(getGrid().get(x,y) == 0){
                    colFull = false;
                    break;
                }
            }
            if(colFull){
                line++;
                col.add(x);
            }
        }
        for(int y = 0; y < getGrid().getRows(); y++){
            boolean rowFull = true;
            for(int x = 0; x < getGrid().getCols(); x++){
                if(getGrid().get(x,y) == 0){
                    rowFull = false;
                    break;
                }
            }
            if(rowFull){
                line++;
                row.add(y);
            }
        }

        logger.info("lines to clear: " + line);
        int blocks = (5*line) - (row.size()*col.size());

        if (line != 0) {
            score(line, blocks);
            setMultiplier(getMultiplier() + 1);
        } else {
            setMultiplier(1);
        }

        if (line > 0) {
            //clear row
            for (int r : row) {
                logger.info("Clear Line");
                for (int i = 0; i < getGrid().getCols(); i++) {
                    getGrid().set(i, r, 0);
                    if(lineClearedListener != null) {
                        lineClearedListener.lineCleared(i,r); //Calls Listener
                    }
                }
            }
            //clear colum
            for (int c : col) {
                logger.info("Clear Line");
                for (int i = 0; i < getGrid().getRows(); i++) {
                    getGrid().set(c, i, 0);
                    if(lineClearedListener != null) {
                        lineClearedListener.lineCleared(c,i); //Calls Listener
                    }
                }
            }
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
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

    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    public int getScore(){
        return score.get();
    }

    public IntegerProperty scoreProperty(){
        return score;
    }

    public void setScore(int score){
        this.score.set(score);
    }

    public int getLevel(){
        return level.get();
    }

    public IntegerProperty levelProperty(){
        return level;
    }

    public void setLevel(int level){
        this.level.set(level);
    }

    public int getLife(){
        return life.get();
    }

    public IntegerProperty lifeProperty(){
        return life;
    }

    public int getMultiplier(){
        return multiplier.get();
    }

    public IntegerProperty multiplierProperty(){
        return multiplier;
    }

    public void setMultiplier(int multiplier){
        this.multiplier.set(multiplier);
    }

    public int[][] getCurrentPieceBlocks() {
        return currentPiece.getBlocks();
    }
}