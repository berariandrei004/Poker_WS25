package at.ac.hcw.UI;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Process serverProcess;
    private PokerClient client;

    private ServerMessageListener messageListener;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Texas Holdem Poker");
    }

    public void stopServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            System.out.println("Server wird beendet...");
            serverProcess.destroy();
        }
    }
    public void switchToGeneralLobbyMenu() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("generalLobbyMenu.fxml"));
        Parent root = loader.load();

        GeneralLobbyController controller = loader.getController();
        setMessageListener(controller);

        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        System.out.println("Switched to generalLobby");
    }
    public void switchToMainMenu() throws IOException {
        root = FXMLLoader.load(getClass().getResource("mainMenu.fxml"));
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToJoinMenu() throws IOException {
        root = FXMLLoader.load(getClass().getResource("joinMenu.fxml"));
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToHostLobbyMenu() throws IOException {
        root = FXMLLoader.load(getClass().getResource("hostLobbyMenu.fxml"));
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToPokerTable() {
        PokerTableView tableView = new PokerTableView();
        Parent root = tableView.createView();
        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    public void setServerProcess(Process serverProcess) {
        this.serverProcess = serverProcess;
    }

    public PokerClient getClient() {
        return client;
    }

    public void setMessageListener(ServerMessageListener listener) {
        this.messageListener = listener;
    }
    private void handleServerMessage(String message) {
        if (messageListener != null) {
            messageListener.onServerMessage(message);
        }
    }

    public void connectToServer(String serverIP, int serverPort, String playerName) {
        if (client == null) {
            client = new PokerClient(serverIP, serverPort);
        }

        new Thread(() -> {
            try {
                if (!client.connect()) {
                    System.out.println("Verbindung fehlgeschlagen");
                    return;
                }

                System.out.println("Erfolgreich verbunden");

                Platform.runLater(() -> {
                    try {
                        switchToGeneralLobbyMenu();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // DAUERHAFTES LISTEN
                String message;
                while ((message = client.receiveMessage()) != null) {
                    String finalMessage = message;
                    Platform.runLater(() -> handleServerMessage(finalMessage));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
