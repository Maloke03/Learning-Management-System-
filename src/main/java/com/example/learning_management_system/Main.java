package com.example.learning_management_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    private StackPane root;

    @Override
    public void start(Stage primaryStage) throws Exception {
        root = new StackPane();


        loadView("/views/login.fxml");

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setTitle("Learning Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            root.getChildren().clear();
            root.getChildren().add(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}