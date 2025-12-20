package at.ac.hcw;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    private static SceneController sceneController;
    @Override
    public void start(Stage stage) throws IOException {
        sceneController = new SceneController();
        sceneController.setStage(stage);
        sceneController.switchToMainMenu();
//        Parent root = FXMLLoader.load(getClass().getResource("mainMenu.fxml"));
//        Scene scene = new Scene(root);
//        stage.setTitle("Texas Holdem Poker");
//        stage.setScene(scene);
//        stage.show();
    }
    public static SceneController getSceneController() {
        return sceneController;
    }
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch();
    }
}
