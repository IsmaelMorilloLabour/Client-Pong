import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Main extends Application {

    public static String protocolWS = "ws";
    public static String host = "localhost";
    public static int port = 3000;
    public static int playersConnected = 0;

    /*
     * public static String protocolWS = "wss";
     * public static String host = "serverpong-production.up.railway.app";
     * public static int port = 443;
     */

    public static UtilsWS socketClient;
    private static CtrlGame ctrlGame;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        final int windowWidth = 800;
        final int windowHeight = 600;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        UtilsViews.addView(getClass(), "ViewLogin", "./assets/viewLogin.fxml");
        UtilsViews.addView(getClass(), "ViewGame", "./assets/viewGame.fxml");
        ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");

        Scene scene = new Scene(UtilsViews.parentContainer);
        scene.addEventFilter(KeyEvent.ANY, keyEvent -> {
            ctrlGame.keyEvent(keyEvent);
        });

        stage.setScene(scene);
        stage.onCloseRequestProperty();
        stage.setResizable(false);
        stage.setTitle("M9 Pong");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:./assets/ping-pong.png");
            stage.getIcons().add(icon);
        }
    }

    public static void checkPlayer() {
        socketClient = UtilsWS.getSharedInstance(Main.protocolWS + "://" + Main.host + ":" + Main.port);
        socketClient.onMessage((response) -> {
            JSONObject msgObj = new JSONObject(response);
            CtrlGame.ctrlCanvas.receive(msgObj);

            if (playersConnected == 2) {
                Platform.runLater(() -> {
                    UtilsViews.setView("ViewGame");
                    CtrlGame ctrlGame = (CtrlGame) UtilsViews.getController("ViewGame");
                    ctrlGame.drawingStart();
                });
            }
        });
    }

    @Override
    public void stop() {
        System.exit(1);
        ctrlGame.drawingStop();
        socketClient.close();
        System.exit(1); // Kill all executor services
    }
}
