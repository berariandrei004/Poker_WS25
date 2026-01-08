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
        root = FXMLLoader.load(getClass().getResource("generalLobbyMenu.fxml"));
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

    public void connectToServer(String serverIP, int serverPort) {
        if (client == null) {
            client = new PokerClient(serverIP, serverPort);
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws IOException {
                boolean connected = client.connect();
                if (connected) {
                    System.out.println("Erfolgreich verbunden!!!");
                    Platform.runLater(() -> {
                        try {
                            switchToGeneralLobbyMenu();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.out.println("Verbindung fehlgeschlagen!");
                }
                return null;
            }
        };
        new Thread(task).start();
    }

}
