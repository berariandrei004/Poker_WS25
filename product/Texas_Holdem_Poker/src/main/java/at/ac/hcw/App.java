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
    }
    public static SceneController getSceneController() {
        return sceneController;
    }
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        JoinCodeHandler.IPv4ToJoinCode("192.168.0.200");
//        JoinCodeHandler.joinCodeToIPv4("FRFRAAAB");
        launch();
    }
}
