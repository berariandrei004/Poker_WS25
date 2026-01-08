package at.ac.hcw.UI;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class JoinMenuController {
    @FXML
    private TextField joinCodeField;
    private PokerClient client;

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
        connectToServer(serverIP, serverPort);
    }
    @FXML
    private void onBackClicked() throws IOException {
        App.getSceneController().switchToMainMenu();
    }

    public void connectToServer(String serverIP, Integer serverPort) {
        client = new PokerClient(serverIP, serverPort);

        // Verbindung im Hintergrund-Thread, damit UI nicht einfriert
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                boolean connected = client.connect();
                if (connected) {
                    System.out.println("Erfolgreich verbunden!");
                    // switch to LobbyMenu in the future
                } else {
                    System.out.println("Verbindung fehlgeschlagen!");
                }
                return null;
            }
        };
        new Thread(task).start();
    }
}
