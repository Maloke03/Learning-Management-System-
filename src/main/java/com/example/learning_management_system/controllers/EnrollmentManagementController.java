package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EnrollmentManagementController {

    @FXML private ComboBox<String> studentComboBox;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private ListView<String> enrolledCoursesList;

    @FXML
    public void initialize() {
        loadStudents();
        loadCourses();

        studentComboBox.setOnAction(e -> loadEnrolledCourses());
    }

    private void loadStudents() {
        ObservableList<String> students = FXCollections.observableArrayList();
        String sql = "SELECT username FROM users WHERE role = 'Student'";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                students.add(rs.getString("username"));
            }
            studentComboBox.setItems(students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCourses() {
        ObservableList<String> courses = FXCollections.observableArrayList();
        String sql = "SELECT id, title FROM courses";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                courses.add(id + " - " + title);
            }
            courseComboBox.setItems(courses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadEnrolledCourses() {
        enrolledCoursesList.getItems().clear();
        String selectedStudent = studentComboBox.getValue();
        if (selectedStudent == null) return;

        String sql = """
                SELECT c.title FROM courses c
                JOIN enrollments e ON c.id = e.course_id
                WHERE e.username = ?
                """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedStudent);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                enrolledCoursesList.getItems().add(rs.getString("title"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnroll() {
        String student = studentComboBox.getValue();
        String course = courseComboBox.getValue();

        if (student == null || course == null) return;

        int courseId = Integer.parseInt(course.split(" - ")[0]);


        String checkSql = "SELECT 1 FROM enrollments WHERE username = ? AND course_id = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, student);
            checkStmt.setInt(2, courseId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Duplicate Enrollment");
                alert.setHeaderText(null);
                alert.setContentText("This student is already enrolled in the selected course.");
                alert.showAndWait();
                return;
            }


            String sql = "INSERT INTO enrollments (username, course_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student);
                stmt.setInt(2, courseId);
                stmt.executeUpdate();
            }

            loadEnrolledCourses();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleUnenroll() {
        String selectedStudent = studentComboBox.getValue();
        String selectedCourseTitle = enrolledCoursesList.getSelectionModel().getSelectedItem();

        if (selectedStudent == null || selectedCourseTitle == null) {
            return;
        }

        String sql = """
        DELETE FROM enrollments 
        WHERE username = ? 
        AND course_id = (
            SELECT id FROM courses WHERE title = ?
        )
    """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, selectedStudent);
            stmt.setString(2, selectedCourseTitle);
            stmt.executeUpdate();

            loadEnrolledCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) studentComboBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
