package at.ac.hcw.UI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;

public class GeneralLobbyController implements ServerMessageListener{
    @FXML private TextField joinCodeShowField;
    @FXML private Label playerCountLabel;
    @FXML private TextField bigBlindField;
    @FXML private TextField smallBlindField;
    @FXML private TextField startingCashField;
    @FXML private ListView<String> playerListView;
    @FXML private Button startGameButton;

    private final ObservableList<String> players = FXCollections.observableArrayList();


    @FXML
    private void onLobbyLeftClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
        App.getSceneController().stopServer();
        PokerClient client = App.getSceneController().getClient();
        client.disconnect();
    }
    @FXML
    private void onStartGameClicked() throws IOException  {
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

        if (message.startsWith("LobbySettings:")) {
            String payload = message.split(":", 2)[1];

            // Mit Semikolon trennen
            String[] parts = payload.split(";");

            if (parts.length == 5) {
                String lobbyId = parts[0];
                String bigBlind = parts[1];
                String smallBlind = parts[2];
                String startingCash = parts[3];
                String playerCount = parts[4];

                // GUI aktualisieren (Platform.runLater falls notwendig)
                Platform.runLater(() -> {
                    joinCodeShowField.setText(lobbyId);
                    bigBlindField.setText(bigBlind);
                    smallBlindField.setText(smallBlind);
                    startingCashField.setText(startingCash);
                    playerCountLabel.setText(playerCount);
                });
            } else {
                System.out.println("Fehler: LobbySettings Nachricht ungültig: " + message);
            }

        } else if (message.startsWith("PlayerList:")) {
            String listStr = message.substring("PlayerList:".length());
            String[] players = listStr.split(";");
            playerListView.getItems().setAll(players); // GUI auf die aktuelle Liste setzen
            if (players.length == Integer.parseInt(playerCountLabel.getText())) {
                startGameButton.setVisible(true);
            }
        } else if (message.startsWith("PlayerJoined:")) {
            String newPlayer = message.split(":", 2)[1];
            if (!playerListView.getItems().contains(newPlayer)) {
                playerListView.getItems().add(newPlayer);
            }
        } else if (message.startsWith("PlayerLeft:")) {
            playerListView.getItems().remove(message.split(":", 2)[1]);
        }
    }
}
