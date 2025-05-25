package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Button toggleButton;
    @FXML private Button actionButton;
    private boolean isLoginMode = true;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Student", "Admin");
        roleComboBox.setValue("Student");
        updateForm();
    }

    @FXML
    private void handleAction(ActionEvent event) {
        if (isLoginMode) {
            handleLogin();
        } else {
            handleRegister();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT password, role FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                String role = rs.getString("role");

                if (BCrypt.checkpw(password, hashedPassword)) {
                    if (!role.equals(selectedRole)) {
                        errorLabel.setText("Role mismatch.");
                        return;
                    }

                    String fxmlFile = role.equals("Admin")
                            ? "/views/admin_dashboard.fxml"
                            : "/views/student_dashboard.fxml";

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                    Scene scene = new Scene(loader.load());
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle(role + " Dashboard");

                    if (role.equals("Student")) {
                        StudentDashboardController controller = loader.getController();
                        controller.setStudentUsername(username);
                    }
                } else {
                    errorLabel.setText("Invalid password.");
                }
            } else {
                errorLabel.setText("User not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database error.");
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            String checkSql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                errorLabel.setText("Username already exists.");
                return;
            }

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, hashed);
            insertStmt.setString(3, role);
            insertStmt.executeUpdate();

            errorLabel.setText("Registration successful. Switch to login.");
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Registration failed.");
        }
    }

    @FXML
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateForm();
    }

    private void updateForm() {
        if (isLoginMode) {
            actionButton.setText("Login");
            toggleButton.setText("Switch to Register");
        } else {
            actionButton.setText("Register");
            toggleButton.setText("Switch to Login");
        }
    }
}
