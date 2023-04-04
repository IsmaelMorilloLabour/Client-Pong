import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

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

    public static CtrlGameCanvas ctrlCanvas = new CtrlGameCanvas();

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

    public void drawingStart() {
        ctrlCanvas.start(canvas);
    }

    public void drawingStop() {
        ctrlCanvas.stop();
    }

    public void updateCanvasSize() {

        final double width = UtilsViews.parentContainer.getWidth();
        final double height = UtilsViews.parentContainer.getHeight();

        // Set Canvas size
        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    public void keyEvent(KeyEvent evt) {
        try {

            JSONObject obj = new JSONObject("{}");
            obj.put("type", "currentDirection");

            // Quan apretem una tecla
            if (evt.getEventType() == KeyEvent.KEY_PRESSED) {
                if (evt.getCode() == KeyCode.UP) {
                    ctrlCanvas.enemyDirection = "up";
                    obj.put("enemyInputDirection", "up");
                }
                if (evt.getCode() == KeyCode.DOWN) {
                    ctrlCanvas.enemyDirection = "down";
                    obj.put("enemyInputDirection", "down");
                }

                if (evt.getCode() == KeyCode.W) {
                    ctrlCanvas.playerDirection = "up";
                    obj.put("playerInputDirection", "up");
                }
                if (evt.getCode() == KeyCode.S) {
                    ctrlCanvas.playerDirection = "down";
                    obj.put("playerInputDirection", "down");
                }
            }

            // Quan deixem anar la tecla
            if (evt.getEventType() == KeyEvent.KEY_RELEASED) {
                if (evt.getCode() == KeyCode.UP) {
                    if (ctrlCanvas.enemyDirection.equals("up")) {
                        ctrlCanvas.enemyDirection = "none";
                        obj.put("enemyInputDirection", "none");
                    }
                }
                if (evt.getCode() == KeyCode.DOWN) {
                    if (ctrlCanvas.enemyDirection.equals("down")) {
                        ctrlCanvas.enemyDirection = "none";
                        obj.put("enemyInputDirection", "none");
                    }
                }
                if (evt.getCode() == KeyCode.W) {
                    if (ctrlCanvas.playerDirection.equals("up")) {
                        ctrlCanvas.playerDirection = "none";
                        obj.put("playerInputDirection", "none");
                    }
                }
                if (evt.getCode() == KeyCode.S) {
                    if (ctrlCanvas.playerDirection.equals("down")) {
                        ctrlCanvas.playerDirection = "none";
                        obj.put("playerInputDirection", "none");
                    }
                }
            }

            Main.socketClient.safeSend(obj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}