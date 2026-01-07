package at.ac.hcw;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Texas Holdem Poker");
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
}
