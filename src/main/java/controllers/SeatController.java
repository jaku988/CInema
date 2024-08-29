package controllers;

import com.sun.tools.javac.Main;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import models.Seat;
import utils.Database;
import utils.UserSession;
import controllers.MainController;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

public class SeatController implements Initializable {
    @FXML
    private GridPane seatGrid;
    @FXML
    private Button reserveButton;
    MainController mainController;

    private int salaId;
    private int seansId;
    private List<Seat> seats;
    private Map<Integer, Integer> seatReservations;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reserveButton.setVisible(UserSession.isLoggedIn());
    }

    public void initData(int salaId, int seansId) {
        this.salaId = salaId;
        this.seansId = seansId;
        loadSeats();
    }

    // metoda odpowiadająca za załadowanie foteli po otworzeniu okna
    private void loadSeats() {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                seats = getSeatsForScreening(salaId);
                seatReservations = getSeatReservations(seansId);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            seatGrid.getChildren().clear();
            for (Seat seat : seats) {
                Pane seatPane = new Pane();
                seatPane.setPrefSize(30, 30);
                seatPane.setStyle("-fx-background-color: " + getSeatColor(seat) + ";");

                seatPane.setOnMouseClicked(event -> {
                    if (seatPane.getStyle().contains("green")) {
                        seatPane.setStyle("-fx-background-color: blue;");
                    } else if (seatPane.getStyle().contains("blue")) {
                        seatPane.setStyle("-fx-background-color: green;");
                    }
                });

                seatGrid.add(seatPane, seat.getNumber() - 1, seat.getRow() - 1);
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    // tutaj ustawiamy odpowiedni kolor dla podanego fotela
    private String getSeatColor(Seat seat) {

        if (seatReservations.containsKey(seat.getId())) {
            int reservedUserId = seatReservations.get(seat.getId());
            int currentUserId = UserSession.getUserId();
            if (reservedUserId == currentUserId) {
                return "yellow";
            } else {
                return "red";
            }
        }
        return "green";
    }

    //aby metoda loadSeats mogła załadować fotele na ekranie, tutaj wczytujemy je z bazy danych
    private List<Seat> getSeatsForScreening(int screeningId) {

        List<Seat> seats = new ArrayList<>();
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT * FROM SEATS WHERE SALA_ID = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, salaId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Seat seat = new Seat();
                        seat.setId(resultSet.getInt("id"));
                        seat.setCinemaId(resultSet.getInt("sala_id"));
                        seat.setRow(resultSet.getInt("rzad"));
                        seat.setNumber(resultSet.getInt("miejsce"));
                        seats.add(seat);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    // wczytujemy listę już zarezerwowanych siedzeń aby w metodzie loadSeats wiedzieć które fotele wczytać na inny kolor
    private Map<Integer, Integer> getSeatReservations(int screeningId) {

        Map<Integer, Integer> seatReservations = new HashMap<>();
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT * FROM reservations WHERE seans_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, screeningId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int seatId = resultSet.getInt("miejsce_id");
                        int userId = resultSet.getInt("użytkownik_id");
                        seatReservations.put(seatId, userId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seatReservations;
    }

    // tworzymy listę foteli które użytkownik zaznaczył na niebiesko a następnie wywołujemy funkcję wpisującą zaznaczone siedzenia do bazy danych
    @FXML
    private void reserveSeats() {

        List<Seat> selectedSeats = new ArrayList<>();
        for (javafx.scene.Node node : seatGrid.getChildren()) {
            if (node instanceof Pane) {
                Pane seatPane = (Pane) node;
                if (seatPane.getStyle().contains("blue")) {
                    int row = GridPane.getRowIndex(seatPane) + 1;
                    int number = GridPane.getColumnIndex(seatPane) + 1;
                    Seat seat = getSeatByPosition(row, number);
                    if (seat != null) {
                        selectedSeats.add(seat);
                    }
                }
            }
        }

        saveReservations(selectedSeats);
        mainController.loadUserReservations();
    }

    private Seat getSeatByPosition(int row, int number) {
        for (Seat seat : seats) {
            if (seat.getRow() == row && seat.getNumber() == number) {
                return seat;
            }
        }
        return null;
    }

    //wpisanie fotela do bazy danych
    private void saveReservations(List<Seat> selectedSeats) {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection connection = Database.getConnection()) {
                    String query = "INSERT INTO reservations (użytkownik_id, seans_id, miejsce_id) VALUES (?, ?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        for (Seat seat : selectedSeats) {
                            preparedStatement.setInt(1, UserSession.getUserId());
                            preparedStatement.setInt(2, seansId); // Using seansId instead of salaId
                            preparedStatement.setInt(3, seat.getId());
                            preparedStatement.addBatch();
                        }
                        preparedStatement.executeBatch();

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                mainController.loadUserReservations();
                loadSeats();
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };

        new Thread(task).start();
    }
}
