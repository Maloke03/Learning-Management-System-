package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class StudentDashboardController {

    @FXML
    private ListView<String> courseList;
    @FXML
    private VBox courseContainer;


    private String studentUsername;

    public void setStudentUsername(String username) {
        this.studentUsername = username;
        loadCourses();
    }


    private void loadCourses() {
        courseContainer.getChildren().clear();

        String query = """
        SELECT c.id, c.title, c.duration, 
               COALESCE(p.progress, 0) AS progress
        FROM courses c
        JOIN enrollments e ON c.id = e.course_id
        LEFT JOIN course_progress p ON p.course_id = c.id AND p.username = ?
        WHERE e.username = ?
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, studentUsername);
            stmt.setString(2, studentUsername);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int courseId = rs.getInt("id");
                String title = rs.getString("title");
                String duration = rs.getString("duration");
                double progress = rs.getDouble("progress");

                Label label = new Label(title + " (" + duration + ")");
                ProgressBar progressBar = new ProgressBar(progress);
                Button startButton = new Button("Start Course");

                startButton.setOnAction(e -> updateProgress(courseId, progress + 0.25));

                VBox courseBox = new VBox(5, label, progressBar, startButton);
                courseBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray;");
                courseContainer.getChildren().add(courseBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(int courseId, double newProgress) {
        double clamped = Math.min(newProgress, 1.0);

        String upsert = """
        INSERT INTO course_progress (username, course_id, progress)
        VALUES (?, ?, ?)
        ON CONFLICT (username, course_id)
        DO UPDATE SET progress = EXCLUDED.progress;
        """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(upsert)) {

            stmt.setString(1, studentUsername);
            stmt.setInt(2, courseId);
            stmt.setDouble(3, clamped);
            stmt.executeUpdate();

            loadCourses();


            if (clamped == 1.0) {
                generateCertificate(courseId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateCertificate(int courseId) {
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT title FROM courses WHERE id = ?")) {

            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String courseTitle = rs.getString("title");


                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Certificate");
                fileChooser.setInitialFileName(studentUsername + "_certificate.pdf");
                File file = fileChooser.showSaveDialog(courseContainer.getScene().getWindow());

                if (file == null) return;


                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                document.add(new Paragraph("Certificate of Completion", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("This certifies that"));
                document.add(new Paragraph(studentUsername, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
                document.add(new Paragraph("has successfully completed the course:"));
                document.add(new Paragraph(courseTitle, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Date: " + java.time.LocalDate.now()));

                document.close();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Certificate generated successfully!", ButtonType.OK);
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleComments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/course_comments.fxml"));
            Scene scene = new Scene(loader.load());

            CourseCommentsController controller = loader.getController();
            controller.setUsername(studentUsername);

            Stage stage = new Stage();
            stage.setTitle("Course Comments");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) courseContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login - LMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) courseContainer.getScene().getWindow();
        stage.close();
    }





}
