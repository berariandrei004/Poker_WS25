package at.ac.hcw.UI;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private Process serverProcess;
    private PokerClient client;

    private PokerTableView currentTableView;
    private ServerMessageListener messageListener;

    public PokerClient getClient () {
        return client;
    }

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
        // Neue Instanz der View erstellen
        currentTableView = new PokerTableView();
        Parent root = currentTableView.createView();

        // Controller als Listener setzen, der an die View delegiert
        this.messageListener = currentTableView;

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    public void setServerProcess(Process serverProcess) {
        this.serverProcess = serverProcess;
    }

    public void setMessageListener(ServerMessageListener listener) {
        this.messageListener = listener;
    }

    private void handleServerMessage(String message) {
       System.out.println("[Client received]: " + message); // Debugging

        // Wenn wir im Spiel sind, an die TableView weiterleiten
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

                client.setPlayerName(playerName);
                client.sendMessage("PlayerName:" + playerName);

                // In Lobby wechseln
                Platform.runLater(() -> {
                    try {
                        switchToGeneralLobbyMenu();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Listen Loop
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
