import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class CtrlGame implements Initializable {

    @FXML
    private AnchorPane anchor;

    @FXML
    private Canvas canvas;

    private static CtrlGameCanvas ctrlCanvas = new CtrlGameCanvas();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Initialize canvas responsive size
        UtilsViews.parentContainer.heightProperty().addListener((observable, oldValue, newvalue) -> {
            updateCanvasSize();
        });
        UtilsViews.parentContainer.widthProperty().addListener((observable, oldValue, newvalue) -> {
            updateCanvasSize();
        });
    }

    public void drawingStart () {
        ctrlCanvas.start(canvas);
    }

    public void drawingStop () {
        ctrlCanvas.stop();
    }

    public void updateCanvasSize () {

        final double width = UtilsViews.parentContainer.getWidth();
        final double height = UtilsViews.parentContainer.getHeight();

        // Set Canvas size
        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    public void keyEvent (KeyEvent evt) {

        // Quan apretem una tecla
        if (evt.getEventType() == KeyEvent.KEY_PRESSED) {
            if (evt.getCode() == KeyCode.LEFT) {
                ctrlCanvas.playerDirection = "left";
            }
            if (evt.getCode() == KeyCode.RIGHT) {
                ctrlCanvas.playerDirection = "right";
            }
        }

        // Quan deixem anar la tecla
        if (evt.getEventType() == KeyEvent.KEY_RELEASED) {
            if (evt.getCode() == KeyCode.LEFT) {
                if (ctrlCanvas.playerDirection.equals("left")) {
                    ctrlCanvas.playerDirection = "none";
                }
            }
            if (evt.getCode() == KeyCode.RIGHT) {
                if (ctrlCanvas.playerDirection.equals("right")) {
                    ctrlCanvas.playerDirection = "none";
                }
            }
        }
    }
}