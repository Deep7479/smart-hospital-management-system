package com.hospital;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Manages the single SQLite connection and one-time schema/seed setup. */
public final class Database {

    private static final String DB_FILE = "hospital.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;
    private static Connection connection;

    private Database() {}

    public static synchronized Connection get() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found on classpath", e);
            }
            connection = DriverManager.getConnection(URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /** Creates tables (schema.sql) and seeds a few doctors/patients if the DB is empty. */
    public static void initialize() {
        try (Statement stmt = get().createStatement()) {
            String schema = readResource("/schema.sql");
            for (String block : schema.split(";")) {
                String trimmed = block.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            seedIfEmpty();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void seedIfEmpty() throws SQLException {
        try (Statement stmt = get().createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM doctors");
            rs.next();
            if (rs.getInt(1) > 0) {
                return; // already seeded
            }
            stmt.executeUpdate("""
                INSERT INTO doctors (name, specialization) VALUES
                ('Dr. Anjali Rao', 'Cardiology'),
                ('Dr. Sameer Kulkarni', 'Orthopedics'),
                ('Dr. Neha Bhatt', 'Pediatrics'),
                ('Dr. Rohan Deshpande', 'General Medicine')
            """);
            stmt.executeUpdate("""
                INSERT INTO patients (name, age, gender, phone, address) VALUES
                ('Amit Sharma', 34, 'Male', '9876543210', 'Pune'),
                ('Sunita Verma', 29, 'Female', '9812345678', 'Mumbai'),
                ('Ravi Kumar', 45, 'Male', '9900112233', 'Nagpur')
            """);
        }
    }

    private static String readResource(String path) throws IOException {
        InputStream in = Database.class.getResourceAsStream(path);
        if (in == null) {
            throw new IOException("Resource not found: " + path);
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--")) {
                    sb.append(line).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
