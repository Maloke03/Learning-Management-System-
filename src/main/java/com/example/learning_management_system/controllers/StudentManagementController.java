package com.example.learning_management_system.controllers;

import com.example.learning_management_system.utils.StudentDAO;
import com.example.learning_management_system.database.DatabaseConnector;
import com.example.learning_management_system.models.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

public class StudentManagementController {

    @FXML
    private TableView<Student> studentTable;

    @FXML
    private TableColumn<Student, Integer> idColumn;

    @FXML
    private TableColumn<Student, String> nameColumn;

    @FXML
    private TableColumn<Student, String> progressColumn;

    private ObservableList<Student> studentList;
    private Connection connection;

    @FXML
    public void initialize() {
        studentList = FXCollections.observableArrayList();
        studentTable.setItems(studentList);
        setupColumns();
        try {
            connection = DatabaseConnector.connect();
            loadStudents();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Connection Error", "Could not connect to the database.");
        }
    }

    private void setupColumns() {
        studentTable.setEditable(true);

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        idColumn.setEditable(false);

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Student student = event.getRowValue();
            student.setName(event.getNewValue());
            updateStudentInDatabase(student);
        });

        progressColumn.setCellValueFactory(cellData -> cellData.getValue().progressProperty());
        progressColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        progressColumn.setOnEditCommit(event -> {
            Student student = event.getRowValue();
            student.setProgress(event.getNewValue());
            updateStudentInDatabase(student);
        });
    }

    private void loadStudents() {
        try {
            StudentDAO studentDAO = new StudentDAO(connection);
            studentList.clear();
            studentList.addAll(studentDAO.getAllStudents());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddStudent(ActionEvent event) {

        Student newStudent = new Student(0, "", "0.0");
        studentList.add(newStudent);
        studentTable.getSelectionModel().select(newStudent);
        studentTable.scrollTo(newStudent);
        int index = studentList.indexOf(newStudent);
        studentTable.edit(index, nameColumn);
    }

    @FXML
    private void handleEditStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("No Student Selected", "Please select a student to edit.");
            return;
        }

        int index = studentList.indexOf(selectedStudent);
        studentTable.edit(index, nameColumn);
    }

    @FXML
    private void handleDeleteStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("No Student Selected", "Please select a student to delete.");
            return;
        }

        try {
            StudentDAO studentDAO = new StudentDAO(connection);
            studentDAO.deleteStudent(selectedStudent.getId());
            studentList.remove(selectedStudent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) studentTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStudentInDatabase(Student student) {
        try {
            StudentDAO studentDAO = new StudentDAO(connection);
            studentDAO.updateStudent(student);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Update Error", "Failed to update student.");
        }
    }
}