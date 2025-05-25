package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseCommentsController {

    @FXML private ComboBox<String> courseComboBox;
    @FXML private ListView<String> commentListView;
    @FXML private TextArea commentTextArea;

    private String currentUsername; // set this from login

    public void setUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    public void initialize() {
        loadCourses();

        courseComboBox.setOnAction(e -> loadComments());
    }

    private void loadCourses() {
        ObservableList<String> courses = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, title FROM courses");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                courses.add(rs.getInt("id") + " - " + rs.getString("title"));
            }
            courseComboBox.setItems(courses);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadComments() {
        commentListView.getItems().clear();
        String selectedCourse = courseComboBox.getValue();
        if (selectedCourse == null) return;

        int courseId = Integer.parseInt(selectedCourse.split(" - ")[0]);

        String sql = "SELECT username, comment, created_at FROM course_comments WHERE course_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            List<String> comments = new ArrayList<>();
            while (rs.next()) {
                String username = rs.getString("username");
                String comment = rs.getString("comment");
                Timestamp time = rs.getTimestamp("created_at");
                comments.add("[" + time + "] " + username + ": " + comment);
            }
            commentListView.setItems(FXCollections.observableArrayList(comments));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePostComment() {
        String comment = commentTextArea.getText().trim();
        String selectedCourse = courseComboBox.getValue();
        if (comment.isEmpty() || selectedCourse == null || currentUsername == null) return;

        int courseId = Integer.parseInt(selectedCourse.split(" - ")[0]);

        String sql = "INSERT INTO course_comments (username, course_id, comment) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currentUsername);
            stmt.setInt(2, courseId);
            stmt.setString(3, comment);
            stmt.executeUpdate();

            commentTextArea.clear();
            loadComments();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/student_dashboard.fxml"));
            Scene scene = new Scene(loader.load());


            StudentDashboardController controller = loader.getController();
            controller.setStudentUsername(currentUsername);

            Stage stage = (Stage) commentListView.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Student Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
