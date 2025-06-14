package org.example.mappers;

import org.example.db.IngredientsDb;
import org.example.domain.BasicProduct;
import org.example.enumeration.Size;
import org.example.enumeration.Type;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BasicProductMapper {
    IngredientsDb ingredientsDb = new IngredientsDb();

    public BasicProduct mapSqlResponseToBasicProduct(final ResultSet rs) throws SQLException {


        if (!rs.next()) {
            return null;
        }

        return BasicProduct.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .image(rs.getString("image"))
                .price(rs.getBigDecimal("price"))
                .type(Type.valueOf(rs.getString("type")
                        .toUpperCase()))
                .size(Size.valueOf(rs.getString("size")
                        .toUpperCase()))
                .build();
    }
}
