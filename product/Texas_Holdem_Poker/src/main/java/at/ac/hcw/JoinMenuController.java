package at.ac.hcw;

import javafx.fxml.FXML;

import java.io.IOException;

public class JoinMenuController {
    @FXML
    private void onBackClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }
}
