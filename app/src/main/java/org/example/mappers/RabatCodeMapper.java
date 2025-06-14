package org.example.mappers;

import org.example.domain.RabatCode;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RabatCodeMapper {

    public RabatCode mapSqlResponseToRabatCode(final ResultSet rs) throws SQLException {
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet cannot be null");
        }

        if (rs.next()) {
            RabatCode rabatCode = RabatCode.builder()
                    .id(rs.getLong(1))
                    .code(rs.getString(2))
                    .rabat(rs.getDouble(3))
                    .startTime(rs.getTimestamp(4).toLocalDateTime())
                    .endTime(rs.getTimestamp(5).toLocalDateTime())
                    .build();
            return rabatCode;
        }

        return null;
    }



}
