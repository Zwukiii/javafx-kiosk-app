package org.example.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseInitializer {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/smartkiosk";
    private static final String DB_USER = "my_user";
    private static final String DB_PASSWORD = "password123";

    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());


    public Connection connection() throws SQLException {
        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD);
    }


    public void initialize() {
        try (Connection conn = connection()) {
            runSqlScript(conn);
            logger.info("Database initialized successfully");
        } catch (SQLException | IOException e) {
            logger.log(Level.SEVERE, "Error during database initialization", e);
        }
    }

    private void runSqlScript(Connection conn) throws IOException, SQLException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("input2.sql")) {
            if (in == null) {
                throw new FileNotFoundException("SQL file input.sql not found in classpath");
            }

            String sqlScript = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            List<String> statements = parseSqlScript(sqlScript);

            try (Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        stmt.execute(statement);
                    }
                }
            }
        }
    }

    private List<String> parseSqlScript(String sqlScript) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        boolean inDollarQuote = false;
        int length = sqlScript.length();

        for (int i = 0; i < length; i++) {
            char currentChar = sqlScript.charAt(i);

            if (i < length - 1 && currentChar == '$' && sqlScript.charAt(i + 1) == '$') {
                inDollarQuote = !inDollarQuote;
                currentStatement.append(currentChar).append(sqlScript.charAt(i + 1));
                i++;
                continue;
            }

            if (currentChar == ';' && !inDollarQuote) {
                String statement = currentStatement.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                currentStatement.setLength(0);
            } else {
                currentStatement.append(currentChar);
            }
        }

        String remaining = currentStatement.toString().trim();
        if (!remaining.isEmpty()) {
            statements.add(remaining);
        }

        return statements;
    }

}
