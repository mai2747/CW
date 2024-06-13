package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ScoresScene extends BaseScene implements CommunicationsListener{

    Game game;
    Multimedia multimedia;
    Communicator communicator;
    ArrayList<VBox> displayLocal = new ArrayList<>();
    ArrayList<VBox> displayRemote = new ArrayList<>();
    ArrayList<Transition> animationLocal = new ArrayList<>();
    ArrayList<Transition> animationRemote = new ArrayList<>();

    private Pair<String,Integer> scoreInfo;
    private int newScore;
    private VBox scorePane;
    private VBox local;
    private VBox online;
    private HBox buttonBox;
    private Button submit;
    private GridPane gridPane;
    private boolean beatLocal;
    private boolean beatOnline;
    private boolean showRanking = false;

    static File file = new File("scoreList.txt");

    private SimpleListProperty<Pair<String,Integer>> localScore;

    private SimpleListProperty<Pair<String,Integer>> remoteScores;

    private ObservableList<Pair<String, Integer>> localRanking;
    private ObservableList<Pair<String,Integer>> onlineRanking = FXCollections.observableArrayList();;

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    public ScoresScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        this.game = game;
        this.newScore = game.getScore();

        multimedia = new Multimedia();
        communicator = gameWindow.getCommunicator();
        communicator.addListener(this);
        showRanking = false;
    }

    @Override
    public void initialise() {
        logger.info("Initialising Score Scene");
        communicator.addListener(message -> Platform.runLater(() -> receiveCommunication(message.trim())));

        scene.setOnKeyPressed(keyEvent -> {
            multimedia.stopMusic();
            gameWindow.startMenu();
        });
        communicator.send("HISCORES");
    }

    /**
     * Check if the score of current game beats the scores in rankings
     * update rankings if needed
     */
    private void checkNewScore(){
        logger.info("Checking new score's ranking...");

        int rankingInOnline = onlineRanking.get(onlineRanking.size()-1).getValue();
        int rankingInLocal = localScore.get(localScore.size()-1).getValue();

        beatOnline = false;
        beatLocal = false;

        if (rankingInOnline < newScore) beatOnline = true;
        if (rankingInLocal < newScore) beatLocal = true;

        if((beatOnline|| beatLocal) && !showRanking) {
            //text field
            TextField name = new TextField();
            name.setPromptText("Enter your name");
            name.setPrefHeight(200);
            name.setPrefWidth((double) gameWindow.getWidth() / 2);
            //name.requestFocus();  //後で試して
            scorePane.getChildren().add(2, name);

            //submit button
            submit = new Button("Submit");
            scorePane.getChildren().add(3, submit);

            var text = new Text("You Got A High Score!!");
            text.getStyleClass().add("title");
            scorePane.getChildren().add(text);

            int localCount = 0;
            int onlineCount = 0;

            if (beatLocal) {
                for (Pair<String, Integer> pair : localRanking) {
                    if (newScore > pair.getValue()) break;
                    localCount++;
                }
            }
            if (beatOnline) {
                for (Pair<String, Integer> pair : onlineRanking) {
                    if (newScore > pair.getValue()) break;
                    onlineCount++;
                }
            }

            int Lcount = localCount;
            int Ocount = onlineCount;

            submit.setOnAction(event -> {
                String name1 = name.getText();

                //update UI to show rankings

                scorePane.getChildren().remove(2);
                scorePane.getChildren().remove(2);

                scoreInfo = new Pair<>(name1, newScore);
                if (beatLocal) {
                    localRanking.add(Lcount, scoreInfo);
                    writeScores(localRanking);

                    updateHiScore(newScore);
                }
                if (beatOnline) {
                    onlineRanking.set(Ocount, scoreInfo);
                    communicator.send("HISCORE " + name1 + ":" + newScore);
                    logger.info("Sent HISCORE info");
                    communicator.send("HISCORES");
                }

                showRanking = true;

                showRanking(localScore, displayLocal,animationLocal, gridPane);
                showRanking(remoteScores, displayRemote, animationRemote,gridPane);
            });
        }else{
            showRanking(localScore, displayLocal,animationLocal, gridPane);
            showRanking(remoteScores, displayRemote, animationRemote,gridPane);
        }
    }

    /**
     * Listen to the signal of "HISCORE" to update remote ranking
     * @param communication the message that was received
     */
    @Override
    public void receiveCommunication(String communication) {
        if(communication.contains("HISCORES")){
            logger.info("Received HISCORES correctly");
            Platform.runLater(() -> updateScore(communication.substring(9)));
              //start reading after "HIGHSCORES"
        }
    }

    /**
     * Update remote score ranking
     * @param score remote scores got from communicator
     */
    private void updateScore(String score){  //online
        logger.info("Updating scores");
        String[] pairs = score.split("\n");
        var scoreList = new ArrayList<Pair<String,Integer>>();

        for(String pair : pairs){
            String[] part = pair.split(":");
            if(part.length == 2){
                String name = part[0];
                int onlineScore = Integer.parseInt(part[1]);
                scoreList.add(new Pair<>(name,onlineScore));
            }
        }
        onlineRanking.addAll(scoreList);
        logger.info("OnlineRanking: " + onlineRanking);

        checkNewScore();
    }

    /**
     * Update local high score if current score beats score in BestScore.txt
     * @param HiScore player's score for current game
     */
    private void updateHiScore(int HiScore){
        String filePath = "BestScore.txt";
        String newContent = String.valueOf(HiScore);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            int bestScore = Integer.parseInt(reader.readLine());

            if(bestScore < HiScore){
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                writer.write(newContent);

                writer.close();
            }

            reader.close();

            System.out.println("High score updated successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while updating the file content: " + e.getMessage());
        }
    }

    /**
     * Set local score ranking
     * @return list that contains scores in local ranking
     */
    public static ArrayList<Pair<String, Integer>> loadScores(){  //local

        ArrayList<Pair<String,Integer>> score = new ArrayList<>();

        logger.info("file exists: "+file.exists());
        if(!file.exists()){
            ArrayList<Pair<String,Integer>> list = new ArrayList<>();
            list.add(new Pair<>("Guest",20000));
            list.add(new Pair<>("Guest",10000));
            list.add(new Pair<>("Guest",8000));
            list.add(new Pair<>("Guest",5000));
            list.add(new Pair<>("Guest",2000));
            list.add(new Pair<>("Guest",1000));
            list.add(new Pair<>("Guest",500));
            list.add(new Pair<>("Guest",300));
            list.add(new Pair<>("Guest",100));
            list.add(new Pair<>("Guest",50));
            writeScores(list);
        }

        try(Scanner s = new Scanner(file)) {
            while (s.hasNext()) {
                String nameScore = s.nextLine();
                String[] info = nameScore.split(",");
                score.add(new Pair<>(info[0], Integer.parseInt(info[1])));
            }
        } catch (FileNotFoundException e) {
            logger.info("Score file not found: " + e);
        }
        return score;
    }

    /**
     * rewrite scoreList.txt to update local score ranking
     * @param list
     */
    public static void writeScores(List<Pair<String, Integer>> list){  //local
        list.sort((score1, score2) -> (score2.getValue().compareTo(score1.getValue())));

        try {
            if (new File("scoreList.txt").createNewFile()) {
                logger.info("File created");
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("File creation error");
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            int count = 0;
            for (Pair<String,Integer> scores : list) {
                count++;
                out.write(scores.getKey() + "," + scores.getValue());
                out.newLine();
                if(count == 10) break;
            }
            out.close();
        }catch (IOException e){
            logger.info("Score writing error: " + e);
        }
    }

    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var ScorePane = new StackPane();
        ScorePane.setMaxWidth(gameWindow.getWidth());
        ScorePane.setMaxHeight(gameWindow.getHeight());
        ScorePane.getStyleClass().add("menu-background");
        root.getChildren().add(ScorePane);

        var mainPane = new BorderPane();
        ScorePane.getChildren().add(mainPane);

        scorePane = new VBox();  //combine all boxes
        scorePane.setAlignment(Pos.CENTER);
        //enteredScore.setPadding(new Insets(10,10,10,10));
        mainPane.setCenter(scorePane);

        var colouredTitle = new Image("C:\\Users\\maika\\OneDrive - University of Southampton\\Programming 2\\coursework\\src\\main\\resources\\images\\TetrECS.png");
        var imageView = new ImageView(colouredTitle);
        imageView.setFitWidth(700);
        imageView.setPreserveRatio(true);
        scorePane.getChildren().add(imageView);

        // Game Over Heading
        var heading = new VBox();
        mainPane.setTop(heading);
        var text = new Text("GAME OVER");
        heading.setAlignment(Pos.TOP_CENTER);
        BorderPane.setMargin(heading, new Insets(20, 0, 0, 0));
        text.getStyleClass().add("bigtitle");
        heading.getChildren().add(text);
        scorePane.getChildren().add(heading);

        //rankings
        local = new VBox();
        var localScores = new Text("Local ranking");
        local.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(local, new Insets(0,0,20,0));
        local.getChildren().add(localScores);
        localScores.getStyleClass().add("heading");

        online = new VBox();
        var onlineScores = new Text("Online ranking");
        online.setAlignment(Pos.CENTER_RIGHT);
        BorderPane.setMargin(online, new Insets(0,20,0,0));
        online.getChildren().add(onlineScores);
        onlineScores.getStyleClass().add("heading");


        gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        //gridPane.visibleProperty();
        gridPane.setVisible(false);

        gridPane.add(local,0,0);  //ranking headings
        gridPane.add(online, 2,0);
        scorePane.getChildren().add(gridPane);

        localRanking = new SimpleListProperty<>(FXCollections.observableArrayList(loadScores()));

        logger.info(localRanking.toString());

        localScore = new SimpleListProperty<>(localRanking);
        remoteScores = new SimpleListProperty<>(onlineRanking);


        //buttons
        var retry = new Text("RETRY");
        retry.getStyleClass().add("menuItem");
        retry.setOnMouseDragEntered(e -> retry.getStyleClass().add("menuItem:hover"));
        retry.setOnMouseClicked(e -> retry());

        var menu = new Text("MENU");
        menu.getStyleClass().add("menuItem");
        menu.setOnMouseDragEntered(e -> menu.getStyleClass().add("menuItem:hover"));
        menu.setOnMouseClicked(e -> startMenu());


        buttonBox = new HBox(30, retry, menu);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        BorderPane.setMargin(buttonBox, new Insets(250,0,50,0));
        scorePane.getChildren().add(buttonBox);
        buttonBox.setVisible(false);


        // Play audio
        multimedia.playAudio("/sounds/explode.wav");
        multimedia.playMusic("/music/end.wav");
    }

    /**
     * Handle system to show rankings with animation
     * @param scores list of scores in rankings
     * @param display list to add the ranking scores
     * @param animation animation to apply when show in the UI
     * @param gridPane pane to add
     */
    private void showRanking(SimpleListProperty<Pair<String,Integer>> scores, ArrayList<VBox> display, ArrayList<Transition> animation, GridPane gridPane){
        int count = 0;

        for(Pair<String, Integer> pair : scores) {
            count++;
            var box = new VBox();
            box.setAlignment(Pos.CENTER);

            var name = new Text(pair.getKey() + ": " + pair.getValue());
            name.getStyleClass().add("scorelist");
            name.setFill(GameBlock.COLOURS[count]);
            box.getChildren().add(name);
            display.add(box);

            if (display == displayLocal) {
                this.gridPane.add(box, 0, count);
                logger.info("Adding local score");
            }else {
                this.gridPane.add(box, 2, count);
                logger.info("Adding online scores");
            }

            if (count == 10) break;
        }
        gridPane.setVisible(true);
        buttonBox.setVisible(true);
        animation(display, animation);
        var transitionRemote = new SequentialTransition(animation.toArray(Animation[]::new));
        transitionRemote.play();
    }

    /**
     * setting for the ranking animation
     * @param boxes list to apply the animation
     * @param animation transition list
     */
    private void animation(ArrayList<VBox> boxes , ArrayList<Transition> animation){
        for (var displayed : boxes) {
            var fade = new FadeTransition(new Duration(50), displayed);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setCycleCount(2);
            animation.add(fade);
        }
    }

    /**
     * call methods when start game again
     */
    public void retry(){
        multimedia.stopMusic();
        multimedia.playAudio("/sounds/lifegain.wav");
        gameWindow.startChallenge();
    }

    /**
     * call methods when return to menu scene
     */
    public void startMenu(){
        multimedia.stopMusic();
        gameWindow.startMenu();
    }
}
