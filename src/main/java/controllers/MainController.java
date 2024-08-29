package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.Database;
import utils.UserSession;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML
    private VBox leftPanel;

    @FXML
    private VBox userPanel;

    @FXML
    private ListView<String> userReservations;

    private String loggedInUser;


    @FXML
    private void loadScreenings(ActionEvent actionEvent) {
        Object userDataObj = actionEvent.getSource();
        if (userDataObj instanceof Button) {
            int salaId = Integer.parseInt(((Button) userDataObj).getUserData().toString());

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/screenings.fxml"));
                Parent root = fxmlLoader.load();
                ScreeningsController screeningsController = fxmlLoader.getController();
                screeningsController.initData(salaId);
                screeningsController.setMainController(this);

                Stage stage = new Stage();
                stage.setTitle("Seanse dla sali " + salaId);
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadUserReservations() {
        userReservations.getItems().clear();
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT s.czas_rozpoczecia || ' - ' || s.czas_zakonczenia || ' - ' || c.nazwa || ' Rząd: ' || se.rzad || ' Miejsce: ' || se.miejsce AS rezerwacja_info " +
                    "FROM CINEMAS c " +
                    "JOIN screenings s ON c.id = s.sala_id " +
                    "JOIN reservations r ON r.seans_id = s.id " +
                    "JOIN seats se ON se.sala_id = s.sala_id AND se.id = r.miejsce_id " +
                    "WHERE r.użytkownik_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, UserSession.getUserId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String reservationInfo = resultSet.getString("rezerwacja_info");
                        userReservations.getItems().add(reservationInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteSelectedReservation(ActionEvent actionEvent) {
        String selectedReservation = userReservations.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            try (Connection connection = Database.getConnection()) {
                String query = "DELETE FROM reservations " +
                        "WHERE użytkownik_id = ? " +
                        "AND miejsce_id = (SELECT se.id " +
                        "FROM CINEMAS c " +
                        "JOIN screenings s ON c.id = s.sala_id " +
                        "JOIN seats se ON se.sala_id = s.sala_id " +
                        "WHERE s.czas_rozpoczecia || ' - ' || s.czas_zakonczenia || ' - ' || c.nazwa || ' Rząd: ' || se.rzad || ' Miejsce: ' || se.miejsce = ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setInt(1, UserSession.getUserId());
                    preparedStatement.setString(2, selectedReservation);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            loadUserReservations();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Brak zaznaczenia");
            alert.setHeaderText("Nie zaznaczono żadnej rezerwacji");
            alert.setContentText("Proszę zaznaczyć rezerwację do usunięcia.");
            alert.showAndWait();
        }
    }

    @FXML
    public void showLogin(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Login");

            LoginController loginController = fxmlLoader.getController();
            loginController.setStage(stage);
            loginController.setMainController(this);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showRegister(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/register.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Register");

            RegisterController registerController = fxmlLoader.getController();
            registerController.setStage(stage);
            registerController.setMainController(this);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout(ActionEvent actionEvent) {
        loggedInUser = null;
        updateUserPanel(null);
        UserSession.logout();
        loadUserReservations();
    }

    public void updateUserPanel(String username) {
        userPanel.getChildren().clear();

        if (username != null) {
            loggedInUser = username;
            Label userLabel = new Label("Zalogowany użytkownik: " + username);
            Button logoutButton = new Button("Wyloguj");
            logoutButton.setOnAction(this::logout);
            userPanel.getChildren().addAll(userLabel, logoutButton);
            loadUserReservations();
        } else {
            Button loginButton = new Button("Zaloguj się");
            loginButton.setOnAction(this::showLogin);
            Button registerButton = new Button("Zarejestruj się");
            registerButton.setOnAction(this::showRegister);
            userPanel.getChildren().addAll(loginButton, registerButton);
        }
    }
}
