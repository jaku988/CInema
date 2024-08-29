package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.setTitle("Aplikacja do rezerwowania miejsc w kinie");

        primaryStage.setWidth(500);
        primaryStage.setHeight(600);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
