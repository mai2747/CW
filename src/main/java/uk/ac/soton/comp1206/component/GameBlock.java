package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private boolean current = false;

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * fade out animation
     */
    AnimationTimer animationTimer;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        //put circle on the centre
        if(gameBoard.getColumnCount() == 3 && gameBoard.getWidth() != 60) {
            if (x == 1 && y == 1) {
                var gc = getGraphicsContext2D();

                double centerX = width / 2.0;
                double centerY = height / 2.0;
                double radius = Math.min(width, height) / 4.0;

                Paint fill = Color.rgb(128, 128, 128, 0.5); // 半透明のグレー
                gc.setFill(fill);

                gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            }
        }

        //hover
        if(this.current){
            var gc = getGraphicsContext2D();
            gc.setFill(Color.rgb(200,200,200,0.3));
            gc.fillRect(0,0,width,height);
        }
    }

    /**
     * Call paint method to paint hovering block
     * @param here block user's cursor is currently entering
     */
    public void hover(boolean here){
        this.current = here;
        paint();
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0, 0, 0.5);  //(0,0, width, height);

        //Border
        gc.setStroke(Color.GRAY);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);


        gc.setFill(Color.rgb(161, 161, 161, 0.5));  // bottom shadow
        gc.fillRect(4, height - 4, width - 4, 4);

        gc.setFill(Color.rgb(161, 161, 161, 0.3));  // left shadow
        gc.fillRect(0, 4, 4, height - 4);

        gc.setFill(Color.rgb(255, 255, 255, 0.5));  // top light
        gc.fillRect(0, 0, width - 4, 4);

        gc.setFill(Color.rgb(255, 255, 255, 0.3));  // right light
        gc.fillRect(width - 4, 0, 4, height - 4);

        gc.setFill(Color.rgb(0, 104, 183, 0.2));
        gc.fillPolygon(new double[]{0,0,width}, new double[]{0,height,height},3);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Call myAnimationTimer to set fade out animation
     */
    public void fadeOut() {
        animationTimer = new myAnimationTimer();
        animationTimer.start();
    }

    /**
     * Inner class for timer animation
     */
    private class myAnimationTimer extends AnimationTimer {
        double fadeOut = 1;

        @Override
        public void handle(long l) {
            {
                paintEmpty();
                var graphic = getGraphicsContext2D();
                fadeOut -= 0.05;

                if (fadeOut <= 0.0) {
                    stop();
                    animationTimer = null;
                }
                graphic.setFill(Color.WHITE.deriveColor(0,0,1,fadeOut));
                graphic.fillRect(0,0,width,height);
            }
        }
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
