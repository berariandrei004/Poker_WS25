package at.ac.hcw.UI;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    private static SceneController sceneController;
    @Override
    public void start(Stage stage) throws IOException {
        sceneController = new SceneController();
        sceneController.setStage(stage);
        sceneController.switchToMainMenu();

        stage.setOnCloseRequest(event -> {
            System.out.println("App wird geschlossen...");
            sceneController.stopServer();
            //System.out.println("Server geschlossen?");
        });
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
