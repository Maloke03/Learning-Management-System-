package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import com.example.learning_management_system.models.Course;
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

public class CourseManagementController {

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> titleColumn;
    @FXML private TableColumn<Course, String> durationColumn;
    @FXML private TextField titleField;
    @FXML private TextField durationField;
    @FXML private TextArea descriptionField;

    private ObservableList<Course> courseList = FXCollections.observableArrayList();

    public void initialize() {
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        durationColumn.setCellValueFactory(data -> data.getValue().durationProperty());

        courseTable.setOnMouseClicked(event -> {
            Course selected = courseTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                titleField.setText(selected.titleProperty().get());
                descriptionField.setText(selected.descriptionProperty().get());
                durationField.setText(selected.durationProperty().get());
            }
        });

        loadCourses();
    }


    private void loadCourses() {
        courseList.clear();
        try (Connection conn = DatabaseConnector.connect();
             var stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM courses")) {

            while (rs.next()) {
                courseList.add(new Course(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("duration")
                ));
            }
            courseTable.setItems(courseList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCourse() {
        String title = titleField.getText();
        String description = descriptionField.getText();
        String duration = durationField.getText();

        if (title.isEmpty() || duration.isEmpty()) return;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO courses (title, description, duration) VALUES (?, ?, ?)")) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, duration);
            stmt.executeUpdate();
            loadCourses();

            titleField.clear();
            descriptionField.clear();
            durationField.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteCourse() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM courses WHERE id = ?")) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            loadCourses();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateCourse() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String newTitle = titleField.getText();
        String newDescription = descriptionField.getText();
        String newDuration = durationField.getText();

        if (newTitle.isEmpty() || newDuration.isEmpty()) return;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE courses SET title = ?, description = ?, duration = ? WHERE id = ?")) {

            stmt.setString(1, newTitle);
            stmt.setString(2, newDescription);
            stmt.setString(3, newDuration);
            stmt.setInt(4, selected.getId());

            stmt.executeUpdate();
            loadCourses();

            titleField.clear();
            descriptionField.clear();
            durationField.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
