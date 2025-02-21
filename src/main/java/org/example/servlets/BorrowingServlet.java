package org.example.servlets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/borrow")
public class BorrowingServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    static class DatabaseConnection {
        private static Connection connection;

        static {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/library", "user", "password");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static Connection getConnection() {
            return connection;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (Statement stmt = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM borrowings")) {
            StringBuilder html = new StringBuilder("<html><body><h1>Borrowings</h1><ul>");
            while (rs.next()) {
                html.append("<li>").append(rs.getString("book_code")).append(" - ").append(rs.getInt("member_id")).append(" - ").append(rs.getDate("borrow_date")).append(" - ").append(rs.getDate("return_date")).append("</li>");
            }
            html.append("</ul></body></html>");
            response.getWriter().write(html.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookCode = request.getParameter("book_code");
        int memberId = Integer.parseInt(request.getParameter("member_id"));

        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement("INSERT INTO borrowings (book_code, member_id, borrow_date) VALUES (?, ?, ?)");) {
            pstmt.setString(1, bookCode);
            pstmt.setInt(2, memberId);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
            response.setStatus(201);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(422);
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String bookCode = request.getParameter("book_code");

        try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement("UPDATE borrowings SET return_date = ? WHERE book_code = ? AND return_date IS NULL");) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, bookCode);
            pstmt.executeUpdate();
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(422);
        }
    }
}