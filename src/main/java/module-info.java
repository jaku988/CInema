module com.example.cinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jdk.compiler;

    opens main to javafx.fxml;
    opens controllers to javafx.fxml;
    opens models to javafx.base;

    exports main;
    exports controllers;
    exports models;
}
