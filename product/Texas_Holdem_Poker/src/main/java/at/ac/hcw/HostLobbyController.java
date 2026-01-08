package at.ac.hcw;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

import java.io.IOException;

public class HostLobbyController {
    @FXML private Spinner<Integer> playerCountSpinner;
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
    @FXML
    private void onStartServerClicked() throws IOException {
        try {
            int maxClients = playerCountSpinner.getValue();
            ProcessBuilder builder = new ProcessBuilder(
                    "java",
                    "-jar",
                    "out/artifacts/Poker_WS25_jar/Poker_WS25.jar",
                    String.valueOf(maxClients)
            );

            builder.inheritIO(); // zeigt Server-Logs in Konsole
            builder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
