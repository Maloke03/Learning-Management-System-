module com.example.learning_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires jbcrypt;
    requires itextpdf;

    opens com.example.learning_management_system to javafx.fxml;
    opens com.example.learning_management_system.controllers to javafx.fxml;

    exports com.example.learning_management_system;
}
