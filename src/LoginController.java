import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController implements Initializable {
    @FXML
    public Label title, inputTitle, waiting;

    @FXML
    public TextField servidor;

    @FXML
    public Button jugar;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    }

    @FXML
    public void connect() {
        Main.checkPlayer();
        if (servidor.getText().isBlank()) {
            servidor.setText("ws://localhost:3000");
        } else {
            String[] server = servidor.getText().split(":");

            Main.protocolWS = server[0];
            Main.host = server[1].replace("//", "");
            Main.port = Integer.parseInt(server[2]);

        }
        showWaiting();
    }

    public void showWaiting() {
        title.setVisible(false);
        inputTitle.setVisible(false);
        servidor.setVisible(false);
        jugar.setVisible(false);
        waiting.setVisible(true);
    }
}
