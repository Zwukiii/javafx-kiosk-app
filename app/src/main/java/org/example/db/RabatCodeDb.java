package org.example.db;

import org.example.domain.RabatCode;
import org.example.mappers.RabatCodeMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RabatCodeDb {

    final private RabatCodeMapper rabatCodeMapper = new RabatCodeMapper();


    public List<RabatCode> getAllAvailableCodes(Connection connection) throws SQLException {
        List<RabatCode> finalList = new ArrayList<>();
        final String ING_QUERY = "SELECT * FROM rabat_code";

        try (PreparedStatement stmt = connection.prepareStatement(ING_QUERY);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                RabatCode rabatCode = RabatCode.builder()
                        .id(rs.getLong("id"))
                        .code(rs.getString("code"))
                        .rabat(rs.getDouble("rabat"))
                        .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                        .endTime(rs.getTimestamp("end_time").toLocalDateTime())
                        .build();
                finalList.add(rabatCode);
            }
        }

        return finalList;
    }

    public RabatCode getCodeByName(Connection connection, String code) throws SQLException {
        final String QUERY = "SELECT * FROM rabat_code WHERE code = ?";

        try (PreparedStatement stmt = connection.prepareStatement(QUERY)) {
            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return RabatCode.builder()
                            .id(rs.getLong("id"))
                            .code(rs.getString("code"))
                            .rabat(rs.getDouble("rabat"))
                            .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                            .endTime(rs.getTimestamp("end_time").toLocalDateTime())
                            .build();
                }
            }
        }

        return null;
    }


    public void createRabatCode(Connection connection, RabatCode rabatCode) throws SQLException {
        final String INSERT_QUERY = "INSERT INTO rabat_code (code, rabat, start_time, end_time) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(INSERT_QUERY)) {
            stmt.setString(1, rabatCode.getCode());
            stmt.setDouble(2, rabatCode.getRabat());
            stmt.setTimestamp(3, Timestamp.valueOf(rabatCode.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(rabatCode.getEndTime()));
            stmt.executeUpdate();
        }
    }


    public void deleteRabatCodeById(Connection connection, long id) throws SQLException {
        final String DELETE_QUERY = "DELETE FROM rabat_code WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(DELETE_QUERY)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    public RabatCode discountDB(Connection connection, String code) throws  SQLException {
        final String query = "SELECT * FROM rabat_code " +
                "WHERE code = ? AND start_time <= NOW() AND end_time >= NOW() ";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, code.trim());
            try (ResultSet rs = stmt.executeQuery())  {
                if (rs.next()) {
                    return RabatCode.builder()
                            .code(rs.getString("code"))
                            .rabat(rs.getDouble("rabat"))
                            .startTime(rs.getTimestamp("start_time").toLocalDateTime())
                            .endTime(rs.getTimestamp("end_time").toLocalDateTime())
                            .build();
                }
            }
            return null;
        }

    }






}
