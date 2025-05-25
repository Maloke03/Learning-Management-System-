package com.example.learning_management_system.controllers;

import com.example.learning_management_system.database.DatabaseConnector;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import java.io.File;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;



public class AnalyticsDashboardController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Pagination pagination;

    private final List<Student> studentList = new ArrayList<>();
    private final int itemsPerPage = 5;

    @FXML
    public void initialize() {
        // ✅ Fetch student data from PostgreSQL
        try (Connection conn = DatabaseConnector.connect();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT name, progress FROM students")) {

            while (rs.next()) {
                String name = rs.getString("name");
                double progress = rs.getDouble("progress");
                studentList.add(new Student(name, progress));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ Set pagination after fetching data
        int pageCount = (int) Math.ceil((double) studentList.size() / itemsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(this::createPage);
    }

    // ✅ Inner class to represent student records
    public static class Student {
        String name;
        double progress;

        public Student(String name, double progress) {
            this.name = name;
            this.progress = progress;
        }
    }

    private VBox createPage(int pageIndex) {
        VBox box = new VBox(15);
        int start = pageIndex * itemsPerPage;
        int end = Math.min(start + itemsPerPage, studentList.size());

        for (int i = start; i < end; i++) {
            Student s = studentList.get(i);

            Label label = new Label(s.name);
            ProgressBar progressBar = new ProgressBar(s.progress);
            progressBar.setPrefWidth(150);

            ProgressIndicator progressIndicator = new ProgressIndicator(s.progress);

            // Start Course button with effects
            Button startButton = new Button("Start Course");
            startButton.setStyle("-fx-font-size: 12;");

            DropShadow shadow = new DropShadow();
            startButton.setEffect(shadow);

            FadeTransition fade = new FadeTransition(Duration.seconds(2), startButton);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            fade.setCycleCount(FadeTransition.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();

            HBox progressRow = new HBox(15, label, progressBar, progressIndicator, startButton);
            progressRow.setStyle("-fx-padding: 5;");
            box.getChildren().add(progressRow);
        }

        scrollPane.setContent(box);
        return box;
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        fileChooser.setInitialFileName("student_progress.csv");

        File file = fileChooser.showSaveDialog(pagination.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Name,Progress\n");
                for (Student s : studentList) {
                    writer.write(s.name + "," + s.progress + "\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "CSV Exported", "Report saved to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not write CSV.");
            }
        }
    }



    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        fileChooser.setInitialFileName("student_progress.pdf");

        File file = fileChooser.showSaveDialog(pagination.getScene().getWindow());
        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                document.add(new Paragraph("Student Progress Report", font));
                document.add(Chunk.NEWLINE);

                PdfPTable table = new PdfPTable(2);
                table.addCell("Name");
                table.addCell("Progress");

                for (Student s : studentList) {
                    table.addCell(s.name);
                    table.addCell(String.valueOf(s.progress));
                }

                document.add(table);
                document.close();

                showAlert(Alert.AlertType.INFORMATION, "PDF Exported", "Report saved to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not write PDF.");
            }
        }
    }



    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    private void handleExit(ActionEvent event) {
        Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        stage.close();
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin_dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) scrollPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
