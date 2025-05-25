package com.example.learning_management_system.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Login - LMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleManageCourses(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/course_management.fxml"));
            Scene scene = new Scene(loader.load());

            Stage courseWindow = new Stage();
            courseWindow.setTitle("Course Management");
            courseWindow.setScene(scene);
            courseWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleManageStudents(ActionEvent event) {
        System.out.println("Manage Students clicked");
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/student_management.fxml"));
            Scene scene = new Scene(loader.load());


            Stage studentWindow = new Stage();
            studentWindow.setTitle("Student Management");
            studentWindow.setScene(scene);
            studentWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewAnalytics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/analyticsdashboard.fxml"));
            Scene scene = new Scene(loader.load());

            Stage analyticsWindow = new Stage();
            analyticsWindow.setTitle("Student Progress Overview");
            analyticsWindow.setScene(scene);
            analyticsWindow.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleManageEnrollments(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/enrollment_management.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Manage Enrollments");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
