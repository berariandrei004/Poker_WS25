package at.ac.hcw;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.io.IOException;

public class HostLobbyController {
    @FXML private Spinner playerCountSpinner;
    @FXML private TextField bigBlindField;
    @FXML private TextField smallBlindField;
    @FXML private TextField startingCashField;
    @FXML
    private void onLobbyCloseClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
    @FXML
    private void onStartGameClicked() throws IOException {
        App.getSceneController().switchToPokerTable();
    }

}
