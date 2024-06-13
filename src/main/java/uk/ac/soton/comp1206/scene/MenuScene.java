package uk.ac.soton.comp1206.scene;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private final Multimedia multimedia;
    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
        this.multimedia = new Multimedia();
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        var colouredTitle = new Image("C:\\Users\\maika\\OneDrive - University of Southampton\\Programming 2\\coursework\\src\\main\\resources\\images\\TetrECS.png");
        var imageView = new ImageView(colouredTitle);
        imageView.setFitWidth(700);
        imageView.setPreserveRatio(true);
        mainPane.setTop(imageView);

        BorderPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);
        BorderPane.setMargin(imageView, new Insets(100,0,0,0));

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(2), imageView);
        translateTransition.setFromY(-20);
        translateTransition.setToY(10);
        translateTransition.setCycleCount(TranslateTransition.INDEFINITE);
        translateTransition.setAutoReverse(true);

        translateTransition.play();

        //buttons

        var play = new Text("PLAY");
        play.getStyleClass().add("menuItem");
        play.setOnMouseDragEntered(e -> play.getStyleClass().add("menuItem:hover"));
        play.setOnMouseClicked(e -> {
            multimedia.stopMusic();
            multimedia.playAudio("/sounds/lifegain.wav");
            gameWindow.startChallenge();
        });

        var multi = new Text("MULTIPLAYER");
        multi.getStyleClass().add("menuItem");
        multi.setOnMouseDragEntered(e -> multi.getStyleClass().add("menuItem:hover"));
        multi.setOnMouseClicked(e -> {
            multimedia.stopMusic();
            gameWindow.startMultiPlayer();
        });

        var howTo = new Text("INSTRUCTION");
        howTo.getStyleClass().add("menuItem");
        howTo.setOnMouseDragEntered(e -> howTo.getStyleClass().add("menuItem:hover"));
        howTo.setOnMouseClicked(e -> {
            multimedia.stopMusic();
            gameWindow.startInstruction();
        });

        var exit = new Text("EXIT");
        exit.getStyleClass().add("menuItem");
        exit.setOnMouseDragEntered(e -> exit.getStyleClass().add("menuItem:hover"));
        exit.setOnMouseClicked(e -> Platform.exit());

        var buttonsBox = new VBox(25, play, multi, howTo, exit);
        buttonsBox.setAlignment(Pos.CENTER);

        StackPane.setAlignment(buttonsBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(buttonsBox, new Insets(300, 0, 0, 0));

        root.getChildren().add(buttonsBox);


        //play music
        multimedia.playMusic("/music/Beloved(chosic.com).mp3");
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising Menu");

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.ESCAPE){
                App.getInstance().shutdown();
            }
        });
    }
}
