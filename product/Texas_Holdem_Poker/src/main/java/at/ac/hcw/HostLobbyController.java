package at.ac.hcw;

import javafx.fxml.FXML;

import java.io.IOException;

public class HostLobbyController {
    @FXML
    private void onLobbyCloseClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
}
