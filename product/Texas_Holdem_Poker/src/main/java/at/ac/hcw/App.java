package at.ac.hcw;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("mainMenu.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1600, 900);
        stage.setTitle("Texas Holdem Poker");
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        launch();
    }
}
