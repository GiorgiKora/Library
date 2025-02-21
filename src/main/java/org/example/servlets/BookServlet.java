package org.example.servlets;

import org.example.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
            // ... (rest of the doGet code remains the same)
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String code = request.getParameter("code");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO books (code, title, author, quantity) VALUES (?, ?, ?, ?)")) {

            // Check for duplicate code before inserting
            if (isDuplicateCode(connection, code)) {
                response.setStatus(422);
                response.getWriter().write("Error: Book with this code already exists.");
                return;
            }

            pstmt.setString(1, code);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setInt(4, quantity);
            pstmt.executeUpdate();
            response.setStatus(201);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(422);
        }
    }

    private boolean isDuplicateCode(Connection connection, String code) throws SQLException {
        try (PreparedStatement checkStmt = connection.prepareStatement("SELECT 1 FROM books WHERE code = ?")) {
            checkStmt.setString(1, code);
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next(); // Returns true if a book with the given code exists
            }
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookCode = request.getPathInfo().substring(1); // Extract book code from path
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE books SET title = ?, author = ?, quantity = ? WHERE code = ?")) {

            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, bookCode);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                response.setStatus(200); // OK - Updated
            } else {
                response.setStatus(404); // Not Found - No book with that code
                response.getWriter().write("Error: Book not found for update.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookCode = request.getPathInfo().substring(1); // Extract book code from path

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("DELETE FROM books WHERE code = ?")) {

            pstmt.setString(1, bookCode);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                response.setStatus(204); // No Content - Deleted successfully
            } else {
                response.setStatus(404); // Not Found
                response.getWriter().write("Error: Book not found for deletion.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
        }
    }
}