package com.example.learning_management_system.models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Progress {
    private final SimpleStringProperty courseTitle;
    private final SimpleDoubleProperty progress;

    public Progress(String title, double progress) {
        this.courseTitle = new SimpleStringProperty(title);
        this.progress = new SimpleDoubleProperty(progress);
    }

    public String getCourseTitle() {
        return courseTitle.get();
    }

    public double getProgress() {
        return progress.get();
    }

    public SimpleStringProperty courseTitleProperty() {
        return courseTitle;
    }

    public SimpleDoubleProperty progressProperty() {
        return progress;
    }
}
