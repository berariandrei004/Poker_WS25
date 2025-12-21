package at.ac.hcw;

import javafx.fxml.FXML;

import java.io.IOException;

public class MainMenuController {
    @FXML
    private void onJoinClicked() throws IOException {
        App.getSceneController().switchToJoinMenu();
    }
    @FXML
    private void onHostClicked() throws IOException {
        App.getSceneController().switchToHostLobbyMenu();
    }
}
