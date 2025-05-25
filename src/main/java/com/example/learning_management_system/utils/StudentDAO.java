package com.example.learning_management_system.utils;

import com.example.learning_management_system.models.Student;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final Connection connection;

    public StudentDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String query = "SELECT * FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                students.add(new Student(rs.getInt("id"), rs.getString("name"),
                        rs.getString("progress")));
            }
        }
        return students;
    }

    public void addStudent(Student student) throws SQLException {
        String query = String.format("INSERT INTO students (name, progress) VALUES ('%s', '%s')",
                student.getName(), student.getProgress());
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    public void updateStudent(Student student) throws SQLException {
        String query = String.format("UPDATE students SET name = '%s', progress = '%s' WHERE id = %d",
                student.getName(), student.getProgress(), student.getId());
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }

    public void deleteStudent(int id) throws SQLException {
        String query = String.format("DELETE FROM students WHERE id = %d", id);
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
        }
    }
}