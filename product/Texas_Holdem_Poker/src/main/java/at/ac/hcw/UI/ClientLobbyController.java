package at.ac.hcw.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ClientLobbyController {
    @FXML private Label playerCount;
    @FXML private TextField bigBlindField;
    @FXML private TextField smallBlindField;
    @FXML private TextField startingCashField;


    @FXML
    private void onLobbyLeftClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
}
