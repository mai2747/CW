package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.List;

public class LobbyScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    Communicator communicator;
    Multimedia multimedia;
    GridPane gridPane;

    private VBox serverList;
    private VBox channelNames;
    private VBox messages;
    private VBox chatMessage;
    private HBox players;
    private Button startGame;
    private ScrollPane scroll;
    protected boolean inChannel;
    private List<String> userList;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
        multimedia = new Multimedia();
        inChannel = false;
        userList = new ArrayList<>();
    }

    /**
     * Handle each signals from communicator
     * @param communication
     */
    public void receiveCommunication(String communication) {
        if(communication.contains("CHANNELS")){
            if(communication.length() > 9) {
                Platform.runLater(() -> showChannels(communication.substring(9)));
            }

        }else if(communication.contains("JOIN") && !inChannel){
            inChannel = true;
            String name = communication.substring(5);  //extract channel name

            //communicator.send("USERS");
            channelBox(name);

        }else if(communication.contains("MSG")){
            String chat[] = communication.substring(4).split(":");
            var message = new Text("<" + chat[0] + ">: " + chat[1]);
            message.getStyleClass().add("messages");
            chatMessage.getChildren().add(message);
            multimedia.playAudio("/sounds/message.wav");
            scroll.setVvalue(1);
            scroll.layout();

        }else if(communication.contains("ERROR")){
            String errorMessage = communication.substring(6); //show error description
            displayError(errorMessage);

        }else if(communication.contains("USERS")){
            String users[] = communication.substring(6).split("\n");
            //userList.clear();
            //userList.addAll(Arrays.asList(users));
            updatePlayer(users);
            logger.info("Received user names");
        }else if(communication.contains("HOST")){
            startGame.setVisible(true);
        }
    }

    /**
     * Handle errors occured
     * @param error Strings about error obtained from communicator
     */
    private void displayError(String error){
        Alert alart = new Alert(Alert.AlertType.ERROR);
        alart.setTitle("Error message");
        alart.setHeaderText("Error");
        alart.setContentText(error);
        alart.showAndWait();
    }

    /**
     * show the list of users names for a channel user's currently in
     * @param userList list of user's names
     */
    public void updatePlayer(String[] userList){
        players.getChildren().clear();
        for(String user : userList){
            var userName = new Text(user + " ");
            userName.getStyleClass().add("instructions");
            players.getChildren().add(userName);
        }
    }

    /**
     * show channels currently opens
     * @param trim names of channels, not trimmed yet
     */
    private void showChannels(String trim){
        channelNames.getChildren().clear();
        String[] channels = trim.split("\n");

        for (String s : channels) {
            var channel = new Text(s);
            channel.getStyleClass().add("channelItem");
            channelNames.getChildren().add(channel);

            channel.setOnMouseClicked(e -> communicator.send("JOIN " + s));
        }
    }

    /**
     * build pane to contain all UI
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var stackPane = new StackPane();
        stackPane.setMaxWidth(gameWindow.getWidth());
        stackPane.setMaxHeight(gameWindow.getHeight());
        stackPane.getStyleClass().add("challenge-background");
        root.getChildren().add(stackPane);

        var mainPane = new BorderPane();
        stackPane.getChildren().add(mainPane);

        gridPane = new GridPane();
        //mainPane.getChildren().add(gridPane);
        mainPane.setCenter(gridPane);

        var title = new Text("Multiplayer");
        title.getStyleClass().add("title");
        BorderPane.setAlignment(title, Pos.TOP_CENTER);
        mainPane.setTop(title);

        //list
        serverList = new VBox();

        var current = new Text("Current Games");
        current.getStyleClass().add("title");

        //text name of new channel
        var createChannel = new HBox();

        var textName = new TextField();
        var submit = new Button("Submit");
        submit.setOnAction(e -> {
            if(textName.getText() != null){
                createChannel.setVisible(false);
                communicator.send("CREATE " + textName.getText());
                textName.clear();
            }
        });

        createChannel.getChildren().addAll(textName,submit);
        createChannel.setVisible(false);

        //button to show textField
        var host = new Text("Host New Game");
        host.getStyleClass().add("heading");
        host.setOnMouseDragEntered(e -> host.getStyleClass().add("menuItem:hover"));  //not working
        host.setOnMouseClicked(e -> createChannel.setVisible(true));

        channelNames = new VBox();  //add channels from server

        serverList.getChildren().addAll(current, host, createChannel, channelNames);
        BorderPane.setAlignment(serverList, Pos.CENTER);
        BorderPane.setMargin(serverList, new Insets(30,0,30,100));
        gridPane.add(serverList,0,1);

        //show chat box
        messages = new VBox();

        mainPane.setRight(messages);
        BorderPane.setAlignment(messages, Pos.CENTER);
        messages.getStyleClass().add("gameBox");
        messages.setPrefWidth((double) gameWindow.getWidth() / 2);
        messages.setMaxHeight((double) gameWindow.getHeight() * 3 / 4);

        messages.setVisible(false);


        multimedia.playMusic("/music/menu.mp3");
    }


    /**
     * Create field and UI of a channel player's currently in
     * @param name channel's name
     */
    public void channelBox(String name){
        messages.getChildren().clear();  //box for chat box

        scroll = new ScrollPane();
        scroll.setFitToHeight(true);
        scroll.maxWidth((double) gameWindow.getWidth() / 2);
        scroll.getStyleClass().add("scroller");

        var channelName = new Text(name);
        channelName.getStyleClass().add("heading");
        //messages.getChildren().add(channelName);  //show channel name

        players = new HBox();

        var description = new Text("Welcome to the lobby\nType \"/nick \" + New Name to change your name");
        description.getStyleClass().add("instructions");

        HBox.setHgrow(messages, Priority.ALWAYS);
        VBox.setVgrow(messages, Priority.ALWAYS);
        VBox.setVgrow(scroll, Priority.ALWAYS);


        startGame = new Button("Start Game");
        startGame.setOnAction(e -> communicator.send("START"));
        startGame.setVisible(false);


        var leaveGame = new Button("Leave Game");
        leaveGame.setOnAction(e -> {
            communicator.send("PART");
            communicator.send("LIST");
            userList.clear();
            messages.getChildren().clear();
            inChannel = false;
            messages.setVisible(false);
            startGame.setVisible(false);
        });

        var chatBox = getTextField();

        var gameButtons = new HBox(220, leaveGame, startGame);
        BorderPane.setAlignment(gameButtons, Pos.BOTTOM_CENTER);

        var sending = new VBox(10, chatBox, gameButtons);

        chatMessage = new VBox();
        chatMessage.getChildren().add(description);

        scroll.setContent(chatMessage);

        messages.getChildren().addAll(channelName, players, scroll, sending);
        messages.setVisible(true);
    }


    /**
     * create textFields in channelBox (just separated channelBox() method to avoid being too long)
     * @return
     */
    private TextField getTextField() {
        var chatBox = new TextField();
        chatBox.setPromptText("Send a message");

        chatBox.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ENTER){
                String text = chatBox.getText();

                String[] split = text.split(" ", 2);
                //change player's name
                if(split[0].equals("/nick") && split.length == 2){
                    communicator.send("NICK " + split[1]);
                }else{
                    communicator.send("MSG " + text);
                }
                chatBox.clear();
            }
        });
        return chatBox;
    }

    @Override
    public void initialise() {
        communicator.addListener(message -> Platform.runLater(() -> receiveCommunication(message.trim())));

        communicator.send("LIST");

        //update current channels every 10 seconds
        Timeline timer = new Timeline(new KeyFrame(Duration.seconds(10), e ->{
            communicator.send("LIST");
            logger.info("Send LIST sign");
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.ESCAPE){
                multimedia.stopMusic();
                gameWindow.startMenu();
            }
        });
    }
}
