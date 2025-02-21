package org.example.servlets;

import org.example.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/members")
public class MemberServlet extends HttpServlet {
    // ... (other code)

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO members (name, email, join_date) VALUES (?, ?, ?)")) {

            // Check for duplicate email
            if (isDuplicateEmail(connection, email)) {
                response.setStatus(422);
                response.getWriter().write("Error: Member with this email already exists.");
                return;
            }

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            response.setStatus(201);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(422);
        }
    }

    private boolean isDuplicateEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement checkStmt = connection.prepareStatement("SELECT 1 FROM members WHERE email = ?")) {
            checkStmt.setString(1, email);
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next(); // Returns true if a member with the given email exists
            }
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int memberId = Integer.parseInt(request.getPathInfo().substring(1)); // Extract member ID
        String name = request.getParameter("name");
        String email = request.getParameter("email");

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE members SET name = ?, email = ? WHERE id = ?")) {

            // Check for duplicate email (excluding the current member being updated)
            if (isDuplicateEmailForUpdate(connection, email, memberId)) {
                response.setStatus(422);
                response.getWriter().write("Error: Member with this email already exists.");
                return;
            }

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, memberId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                response.setStatus(200); // OK - Updated
            } else {
                response.setStatus(404); // Not Found
                response.getWriter().write("Error: Member not found for update.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    private boolean isDuplicateEmailForUpdate(Connection connection, String email, int memberId) throws SQLException {
        try (PreparedStatement checkStmt = connection.prepareStatement("SELECT 1 FROM members WHERE email = ? AND id != ?")) {
            checkStmt.setString(1, email);
            checkStmt.setInt(2, memberId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                return rs.next(); // Returns true if a member with the given email (excluding the current one) exists
            }
        }
    }


    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int memberId = Integer.parseInt(request.getPathInfo().substring(1)); // Extract member ID

        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = connection.prepareStatement("DELETE FROM members WHERE id = ?")) {

            pstmt.setInt(1, memberId);
            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                response.setStatus(204); // No Content
            } else {
                response.setStatus(404); // Not Found
                response.getWriter().write("Error: Member not found for deletion.");
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
}
