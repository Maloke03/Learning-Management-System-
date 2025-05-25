package com.example.learning_management_system.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Course {
    private int id;
    private StringProperty title;
    private StringProperty description;
    private StringProperty duration;

    public Course(int id, String title, String description, String duration) {
        this.id = id;
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.duration = new SimpleStringProperty(duration);
    }

    public int getId() {
        return id;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty durationProperty() {
        return duration;
    }
}
