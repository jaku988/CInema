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

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    private MainController mainController;

    private Stage stage;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try (Connection connection = Database.getConnection()) {
            String query = "SELECT id FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int userId = resultSet.getInt("id");

                        UserSession.login(userId);
                        mainController.updateUserPanel(username);
                        stage.close();
                    } else {

                        System.out.println("Login Failed");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Blad");
                        alert.setHeaderText("Nieprawidlowe dane logowania!");
                        alert.show();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
