import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController implements Initializable{
    @FXML
    public TextField servidor;

    @FXML
    public Button jugar;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

    }

    @FXML
    public void connect() {
        
        if (servidor.getText().isBlank()) {
            servidor.setText("ws://localhost:3000");
            goToGame();
        } else {
            String[] server = servidor.getText().split(":");
            
            Main.protocolWS = server[0];
            Main.host = server[1].replace("//","");
            Main.port = Integer.parseInt(server[2]);
            goToGame();
        }
    }

    @FXML
    private void goToGame() {
        UtilsViews.setViewAnimating("ViewGame");
    }
}
