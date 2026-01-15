package com.example.student;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import com.google.gson.*;
import java.sql.*;

public class StudentServlet extends HttpServlet {
    private String dbUrl;
    private String dbUser;
    private String dbPass;
    public void init() {
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String db = System.getenv("DB_NAME");
        dbUser = System.getenv("DB_USER");
        dbPass = System.getenv("DB_PASSWORD");
        if (port == null || port.isEmpty()) port = "3306";
        dbUrl = "jdbc:mariadb://" + host + ":" + port + "/" + db;
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        try (BufferedReader reader = req.getReader()) {
            Gson gson = new Gson();
            Student s = gson.fromJson(reader, Student.class);
            try (Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                String sql = "INSERT INTO students (name, contact_number, address) VALUES (?, ?, ?)";
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, s.name);
                ps.setString(2, s.contact_number);
                ps.setString(3, s.address);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                int id = -1;
                if (rs.next()) id = rs.getInt(1);
                resp.getWriter().println("Stored id=" + id);
            } catch (SQLException ex) {
                ex.printStackTrace(resp.getWriter());
            }
        }
    }

    private static class Student {
        String name;
        String contact_number;
        String address;
    }
}
