package at.ac.hcw.UI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

public class GeneralLobbyController implements ServerMessageListener{
    @FXML private TextField joinCodeShowField;
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
        PokerClient client = App.getSceneController().getClient();
        client.disconnect();
    }
    @FXML
    public void initialize() {
        playerListView.setItems(players);
    }
    public void setLobbyId(String lobbyId) {
        Platform.runLater(() -> joinCodeShowField.setText(lobbyId));
    }
    public void addPlayer(String playerName) {
        Platform.runLater(() -> players.add(playerName));
    }
    public void removePlayer(String playerName) {
        Platform.runLater(() -> players.remove(playerName));
    }
    @Override
    public void onServerMessage(String message) {

        if (message.startsWith("LobbyId:")) {
            joinCodeShowField.setText(message.split(":", 2)[1]);

        } else if (message.startsWith("PlayerJoined:")) {
            playerListView.getItems().add(message.split(":", 2)[1]);

        } else if (message.startsWith("PlayerLeft:")) {
            playerListView.getItems().remove(message.split(":", 2)[1]);
        }
    }
}
