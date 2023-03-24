import org.json.JSONObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    
    public static String protocolWS = "ws";
    public static String host = "localhost";
    public static int port = 3000;
    /*
        public static String protocolWS = "wss";
        public static String host = "serverpong-production.up.railway.app";
        public static int port = 443;
    */

    public static UtilsWS socketClient;
    private CtrlGame ctrlGame;

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
        System.out.println(ctrlGame);

        Scene scene = new Scene(UtilsViews.parentContainer);
        scene.addEventFilter(KeyEvent.ANY, keyEvent -> { ctrlGame.keyEvent(keyEvent); });
        
        stage.setScene(scene);
        stage.onCloseRequestProperty();
        stage.setTitle("M9 Pong");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> { ctrlGame.drawingStart(); });
        stage.show();

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:./assets/icon.png");
            stage.getIcons().add(icon);
        }

        // Iniciar WebSockets
        socketClient = UtilsWS.getSharedInstance(protocolWS + "://" + host + ":" + port);
        socketClient.onMessage((response) -> {
            // JavaFX necessita que els canvis es facin des de el thread principal
            Platform.runLater(() -> {
                JSONObject msgObj = new JSONObject(response);
                CtrlGame.ctrlCanvas.receiveMessage(msgObj);
            });
        });
    }

    @Override
    public void stop() { 
        ctrlGame.drawingStop();
        System.exit(1); // Kill all executor services
    }
}
