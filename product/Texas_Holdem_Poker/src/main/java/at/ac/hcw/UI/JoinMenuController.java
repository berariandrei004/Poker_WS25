package at.ac.hcw.UI;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class JoinMenuController {
    @FXML
    private TextField joinCodeField;

    @FXML
    private void onJoinButtonClicked() {
        String joinCode = joinCodeField.getText().trim();
        if (joinCode.length() != 8) {
            System.out.println("JoinCode muss 8 Zeichen haben");
            //am besten ein Error ausgabe Feld in der UI implementieren und hier die Errorausgabe in das Feld geben
            return;
        }
        String serverIP = JoinCodeHandler.joinCodeToIPv4(joinCode);
        int serverPort = 5000; // fester Port
        App.getSceneController().connectToServer(serverIP, serverPort);
    }
    @FXML
    private void onBackClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
}
