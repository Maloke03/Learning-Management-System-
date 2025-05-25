package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EnrollmentManagementController {

    @FXML private ComboBox<String> studentComboBox;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private ListView<String> enrolledCoursesList;
    @FXML private TableView<StudentProgress> progressTable;
    @FXML private TableColumn<StudentProgress, String> courseTitleColumn;
    @FXML private TableColumn<StudentProgress, String> progressColumn;

    private final ObservableList<StudentProgress> progressList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // set up table columns
        courseTitleColumn.setCellValueFactory(data -> data.getValue().courseTitleProperty());
        progressColumn    .setCellValueFactory(data -> data.getValue().progressProperty());
        progressTable.setItems(progressList);

        loadStudents();
        loadCourses();

        // when user selects a student, refresh both list and table
        studentComboBox.setOnAction(e -> {
            loadEnrolledCourses();
            loadProgress();
        });
    }

    private void loadStudents() {
        ObservableList<String> students = FXCollections.observableArrayList();
        String sql = "SELECT username FROM users WHERE role = 'Student'";
        try (var conn = DatabaseConnector.connect();
             var stmt = conn.prepareStatement(sql);
             var rs   = stmt.executeQuery()) {

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
        try (var conn = DatabaseConnector.connect();
             var stmt = conn.prepareStatement(sql);
             var rs   = stmt.executeQuery()) {

            while (rs.next()) {
                int    id    = rs.getInt("id");
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
        String student = studentComboBox.getValue();
        if (student == null) return;

        String sql = """
            SELECT c.title
              FROM courses c
              JOIN enrollments e ON c.id = e.course_id
             WHERE e.username = ?
            """;
        try (var conn = DatabaseConnector.connect();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                enrolledCoursesList.getItems().add(rs.getString("title"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProgress() {
        progressList.clear();
        String student = studentComboBox.getValue();
        if (student == null) return;

        String sql = """
            SELECT c.title,
                   COALESCE(p.progress, 0) * 100 AS percent
              FROM courses c
              JOIN enrollments e ON c.id = e.course_id
              LEFT JOIN course_progress p
                ON p.course_id = c.id
               AND p.username = e.username
             WHERE e.username = ?
            """;
        try (var conn = DatabaseConnector.connect();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                String title   = rs.getString("title");
                String percent = String.format("%.0f%%", rs.getDouble("percent"));
                progressList.add(new StudentProgress(title, percent));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEnroll() {
        String student = studentComboBox.getValue();
        String course  = courseComboBox.getValue();
        if (student == null || course == null) return;

        int courseId = Integer.parseInt(course.split(" - ")[0]);

        // prevent duplicate
        String checkSql = """
            SELECT 1
              FROM enrollments
             WHERE username = ? AND course_id = ?
        """;
        try (var conn       = DatabaseConnector.connect();
             var checkStmt  = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, student);
            checkStmt.setInt(2, courseId);
            var rs = checkStmt.executeQuery();
            if (rs.next()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "This student is already enrolled in that course.")
                        .showAndWait();
                return;
            }

            String sql = "INSERT INTO enrollments (username, course_id) VALUES (?, ?)";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, student);
                stmt.setInt(2, courseId);
                stmt.executeUpdate();
            }

            loadEnrolledCourses();
            loadProgress();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUnenroll() {
        String student = studentComboBox.getValue();
        String course  = enrolledCoursesList.getSelectionModel().getSelectedItem();
        if (student == null || course == null) return;

        String sql = """
            DELETE FROM enrollments
             WHERE username = ?
               AND course_id = (SELECT id FROM courses WHERE title = ?)
        """;
        try (var conn = DatabaseConnector.connect();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student);
            stmt.setString(2, course);
            stmt.executeUpdate();

            loadEnrolledCourses();
            loadProgress();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            var loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) studentComboBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // simple inner model for the progress table
    public static class StudentProgress {
        private final SimpleStringProperty courseTitle;
        private final SimpleStringProperty progress;

        public StudentProgress(String courseTitle, String progress) {
            this.courseTitle = new SimpleStringProperty(courseTitle);
            this.progress    = new SimpleStringProperty(progress);
        }

        public StringProperty courseTitleProperty() { return courseTitle; }
        public StringProperty progressProperty()    { return progress;    }
    }
}
