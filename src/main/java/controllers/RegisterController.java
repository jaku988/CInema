package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import utils.Database;
import utils.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button registerButton;

    private MainController mainController;

    private Stage stage;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void register() {
        String username = usernameField.getText();
        String password = passwordField.getText();


        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Brakuje potrzebnych danych!");
            alert.show();
            return;
        }

        boolean userExist = false;

        try (Connection connection = Database.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        userExist = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!userExist) {
            try (Connection connection = Database.getConnection()) {
                String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    preparedStatement.executeUpdate();


                    mainController.updateUserPanel(username);
                    stage.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText("Użytkownik już istnieje!");
            alert.show();
        }
    }

}

