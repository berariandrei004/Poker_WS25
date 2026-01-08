package at.ac.hcw.UI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

public class generalLobbyController {
    @FXML private Label playerCount;
    @FXML private TextField bigBlindField;
    @FXML private TextField smallBlindField;
    @FXML private TextField startingCashField;
    @FXML private ListView<String> playerListView;

    private final ObservableList<String> players = FXCollections.observableArrayList();


    @FXML
    private void onLobbyLeftClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
        App.getSceneController().stopServer();
    }
    @FXML
    public void initialize() {
        playerListView.setItems(players);
    }
    public void addPlayer(String playerName) {
        Platform.runLater(() -> players.add(playerName));
    }
    public void removePlayer(String playerName) {
        Platform.runLater(() -> players.remove(playerName));
    }
}
