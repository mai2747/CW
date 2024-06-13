package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionScene.class);
    Multimedia multimedia = new Multimedia();

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public InstructionScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var instructionPane = new StackPane();
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionPane);

        var mainPane = new BorderPane();
        instructionPane.getChildren().add(mainPane);

        var image = new Image("C:\\Users\\maika\\OneDrive - University of Southampton\\Programming 2\\coursework\\src\\main\\resources\\images\\Instructions.png");
        var imageView = new ImageView(image);

        imageView.setFitWidth(600);
        imageView.setPreserveRatio(true);
        mainPane.setTop(imageView);

        BorderPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);
        BorderPane.setMargin(imageView, new Insets(0));


        var exitButton = new Button("BACK");
        exitButton.setOnAction(actionEvent -> {
            multimedia.stopMusic();
            gameWindow.startMenu();
        });

        exitButton.getStyleClass().add("smallerButton");
        BorderPane.setAlignment(exitButton, javafx.geometry.Pos.TOP_LEFT);
        BorderPane.setMargin(imageView, new Insets(10,10,10,10));
        exitButton.setPrefSize(90,20);

        root.getChildren().add(exitButton);


        var gridPane = new GridPane();
        gridPane.setPrefSize(25,25);
        gridPane.setHgap(5);
        gridPane.setVgap(5);

        //create 15 pieces and set on each pieceBoard
        for (int i = 0; i < GamePiece.PIECES; i++) {
            GamePiece piece = GamePiece.createPiece(i);

            PieceBoard pieceBoard = new PieceBoard(60,60);
            pieceBoard.setPiece(piece);
            mainPane.setCenter(pieceBoard);

            gridPane.add(pieceBoard, i % 5, i / 5);
        }
        BorderPane.setAlignment(gridPane, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(gridPane, new Insets(0,0,0,200));
        mainPane.setCenter(gridPane);
    }


    @Override
    public void initialise() {
        logger.info("Initialising Instruction");

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.ESCAPE){
                multimedia.stopMusic();
                gameWindow.startMenu();
            }
        });

        multimedia.playMusic("/music/menu.mp3");
    }
}
