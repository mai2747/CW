package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene implements NextPieceListener {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    protected Game game;
    protected Multimedia multimedia;
    protected PieceBoard pieceBoard;
    protected PieceBoard followingPieceBoard;

    private int aimX = 0;
    private int aimY = 0;
    protected GameBoard board;
    protected HBox timerBar;
    protected Rectangle timer;
    public IntegerProperty bestScore = new SimpleIntegerProperty();


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");

        multimedia = new Multimedia();
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(), (double) gameWindow.getWidth() / 2, (double) gameWindow.getWidth() / 2);
        board.getStyleClass().add("gameBox");
        mainPane.setCenter(board);

        board.setOnBlockClick(this::blockClicked);

        var scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        var scoreLabel = new Text("Score");
        var score = new Text("0");
        scoreLabel.getStyleClass().add("heading");
        score.textProperty().bind(game.scoreProperty().asString());
        score.getStyleClass().add("score");
        scoreBox.setPadding(new Insets(20,0,0,20));
        scoreBox.getChildren().addAll(new Node[] {scoreLabel, score});

        var levelBox = new VBox();
        var levelLabel = new Text("Levels");
        var level = new Text("0");
        levelBox.setAlignment(Pos.CENTER_LEFT);
        levelLabel.getStyleClass().add("heading");
        level.textProperty().bind(game.levelProperty().asString());
        level.getStyleClass().add("level");
        levelBox.setPadding(new Insets(20,0,0,0));
        levelBox.getChildren().addAll(levelLabel, level);

        var lifeBox = new VBox();
        var lifeLabel = new Text("Live");
        var life = new Text("3");
        lifeBox.setAlignment(Pos.CENTER_RIGHT);
        lifeLabel.getStyleClass().add("heading");
        life.textProperty().bind(game.lifeProperty().asString());
        life.getStyleClass().add("lives");
        lifeBox.setPadding(new Insets(20,0,0,0));
        lifeBox.getChildren().addAll(new Node[]{lifeLabel, life});

        var labelsTop = new HBox(levelBox, scoreBox, lifeBox); //add labels horizontally
        labelsTop.setAlignment(Pos.CENTER);
        labelsTop.setSpacing(250);

        mainPane.setTop(labelsTop);  //set Label


            var BestBox = new VBox();
            var highScore = new Text("High Score");
            highScore.getStyleClass().add("heading");
            var highest = new Text(String.valueOf(getHighScore()));
            BestBox.setAlignment(Pos.CENTER);
            BestBox.getChildren().addAll(highScore,highest);
            highest.getStyleClass().add("hiscore");
            BorderPane.setAlignment(BestBox, Pos.CENTER);
            BestBox.setPadding(new Insets(20,0,0,0));

            pieceBoard = new PieceBoard(160,160);
            BorderPane.setAlignment(pieceBoard, Pos.CENTER_RIGHT);
            var incoming = new Text("Incoming");
            incoming.getStyleClass().add("heading");
            followingPieceBoard = new PieceBoard(100,100);
            BorderPane.setAlignment(followingPieceBoard, Pos.BOTTOM_RIGHT);

            var miniBoards = new VBox(30, BestBox, incoming, pieceBoard, followingPieceBoard);
            miniBoards.setAlignment(Pos.CENTER);
            miniBoards.setPadding((new Insets(0,0,0,20)));
            mainPane.setRight(miniBoards);


        timerBar = new HBox();
        timer = new Rectangle();

        timer.setHeight(10);
        timerBar.getChildren().add(timer);
        mainPane.setBottom(timerBar);

        game.setNextPieceListener(this);
        game.setLineClearedListener(this::clearLine);

        multimedia.playMusic("/music/Komiku-04-Skate.mp3");
    }

    /**
     * set animation to the timer corresponding to the timerDelay
     * @param delay set to calculate the timing to change colour
     */
    protected void timerAnimation(int delay) {
        Timeline timeline = new Timeline();
        KeyValue start = new KeyValue(timer.widthProperty(), timerBar.getWidth());
        timeline.getKeyFrames().add(new KeyFrame(new Duration(0), start));

        KeyValue green = new KeyValue(timer.fillProperty(), Color.GREEN);
        timeline.getKeyFrames().add(new KeyFrame(new Duration(0), green));

        KeyValue yellow = new KeyValue(timer.fillProperty(), Color.YELLOW);
        timeline.getKeyFrames().add(new KeyFrame(new Duration((double) delay / 2), yellow));

        KeyValue red = new KeyValue(timer.fillProperty(), Color.RED);
        timeline.getKeyFrames().add(new KeyFrame(new Duration((double) delay / 2), red));

        KeyValue end = new KeyValue(timer.widthProperty(), 0);
        timeline.getKeyFrames().add(new KeyFrame(new Duration(delay), end));

        timeline.play();
    }


    public int getHighScore(){
        logger.info("Finding best score...");
        var file = new File("BestScore.txt");
        int bestScore = 0;

        if(!file.exists()) bestScoreFile();

        try{
            var s = new Scanner(file);
            bestScore = s.nextInt();
        }catch (IOException e){
            logger.info("File not found:" +e);
        }
        return bestScore;
    }

    /**
     * write the BestScore text file if not exists
     */
    private void bestScoreFile(){
        try {
            if (new File("BestScore.txt").createNewFile()) {
                logger.info("File created");
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("File creation error");
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("BestScore.txt"));
            out.write("0");
            out.close();
        }catch (IOException e){
            logger.info("Score writing error: " + e);
        }
    }


    /**
     * call fadeOut() method via GameBoard to get blocks
     * @param x get column to clear
     * @param y get row to clear
     */
    public void clearLine(int x, int y){
        multimedia.playAudio("/sounds/clear.wav");
        board.fadeOut(x,y);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     * and set event handlers for KEY_PRESSED, mouse clicked and end game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();

        game.setOnGameLoop(this::timerAnimation);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.ESCAPE){
                game.endsTimer();
                gameWindow.cleanup();
                multimedia.stopMusic();
                gameWindow.startMenu();
            }else if(key.getCode() == KeyCode.SPACE || key.getCode() == KeyCode.R){
                logger.info("swap pieces");
                game.swapCurrentPiece();
            }else if((key.getCode() == KeyCode.ENTER || key.getCode() == KeyCode.X)) {
                game.blockClicked(board.getBlock(aimX,aimY));
            }else if(key.getCode() == KeyCode.Q || key.getCode() == KeyCode.Z || key.getCode() == KeyCode.OPEN_BRACKET){
                rotateCurrentPiece(3);
            }else if(key.getCode() == KeyCode.E || key.getCode() == KeyCode.C || key.getCode() == KeyCode.CLOSE_BRACKET){
                rotateCurrentPiece(1);
            }else if(key.getCode() == KeyCode.SHIFT){
                game.nextPiece();
            }else if(key.getCode() == KeyCode.UP || key.getCode() == KeyCode.W){
                if (aimY > 0) aimY--;
                logger.info("Up");
                board.hover(board.getBlock(aimX,aimY));
            }else if(key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.S){
                if (aimY < game.getRows()-1) aimY++;  //should be the length of gameBoard? or 5
                board.hover(board.getBlock(aimX,aimY));
                logger.info("Down");
            }else if(key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.D){
                if (aimX < game.getCols()-1) aimX++;
                board.hover(board.getBlock(aimX,aimY));
                logger.info("Right");
            }else if(key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.A){
                if (aimX > 0) aimX--;
                board.hover(board.getBlock(aimX,aimY));
                logger.info("Left");
            }
        });

        scene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                rotateCurrentPiece(1);
            }
        });

        game.EndsGame(() -> {
            game.endsTimer();
            multimedia.stopMusic();
            gameWindow.startScores(game);
        });

        game.registerNextPieceListener(this);
    }

    /**
     * call method to rotate and put rotated piece to the piece board
     * @param rotation times to rotate
     */
    public void rotateCurrentPiece(int rotation) {
        game.rotateCurrentPiece(rotation);
        multimedia.playAudio("/sounds/pling.wav");
        pieceBoard.setPiece(game.getCurrentPiece());
    }

    /**
     * set next pieces to the piece board
     * @param piece current piece
     * @param followingPiece following piece that can be swapped
     */
    @Override
    public void nextPiece(GamePiece piece, GamePiece followingPiece) {
        pieceBoard.setPiece(piece);
        followingPieceBoard.setPiece(followingPiece);
    }
}
