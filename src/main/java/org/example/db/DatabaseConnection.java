package org.example.db; // Or your package

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws ClassNotFoundException, SQLException {
        // 1. Load the JDBC driver
        Class.forName("org.postgresql.Driver");

        // 2. Establish the connection
        String url = "jdbc:postgresql://localhost:5432/library"; // Database URL
        String user = "user"; // Database username
        String password = "password"; // Database password

        connection = DriverManager.getConnection(url, user, password);
    }

    public static DatabaseConnection getInstance() throws ClassNotFoundException, SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}