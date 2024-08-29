package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import utils.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ScreeningsController {
    @FXML
    private ListView<Button> screeningsListView;

    private int salaId;
    MainController mainController;

    public void setMainController(MainController mainController){
        this.mainController = mainController;
    }

    public void initData(int salaId) {
        this.salaId = salaId;
        loadScreeningsFromDatabase();
    }

    private void loadScreeningsFromDatabase() {
        List<Button> screeningButtons = new ArrayList<>();
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT id, czas_rozpoczecia, czas_zakonczenia FROM Screenings WHERE sala_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, salaId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int screeningId = resultSet.getInt("id");
                        Timestamp startTime = resultSet.getTimestamp("czas_rozpoczecia");
                        Timestamp endTime = resultSet.getTimestamp("czas_zakonczenia");
                        String screeningText = startTime + " - " + endTime;

                        Button screeningButton = new Button(screeningText);
                        screeningButton.setOnAction(event -> openSeatSelection(salaId, screeningId));
                        screeningButtons.add(screeningButton);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        screeningsListView.getItems().addAll(screeningButtons);
    }

    @FXML
    private void openSeatSelection(int salaId, int seansId) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/seats.fxml"));
        try {
            Parent root = loader.load();
            SeatController seatController = loader.getController();
            seatController.initData(salaId, seansId); // Pass both salaId and seansId
            seatController.setMainController(mainController);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setHeight(600);
            stage.setWidth(400);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
